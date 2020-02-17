package r01p.portal.appembed;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.collect.Maps;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.VersionNumber;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
class R01PUserAgentData {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final ReadableUserAgent _userAgent;
	@Getter private final String _userAgentJSVar;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public R01PUserAgentData(final String userAgentString,
							 final ReadableUserAgent userAgent) {
		_userAgent = userAgent;
		_userAgentJSVar = _composeUserAgentJSVar(userAgentString,
												 userAgent);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final static int INITIAL_INDENT = 2;
	static String _composeUserAgentJSVar(final String userAgentString,
										 final ReadableUserAgent userAgent) {		
		Map<String,Object> props = _extractPropertyMap(userAgentString,
													   userAgent);
		Observable<String> obs = _observableFrom(2,		// initial indentation
												 props);
		final StringBuilder outJSVar = new StringBuilder();
		outJSVar.append("{\n");
		obs.subscribe(new Observer<String>() {								
								@Override
								public void onNext(final String s) {
									// WTF! dirty hack to remove extra "," at the last object's properties
									if (s.contains("}") && outJSVar.charAt(outJSVar.length()-2) == ',') {
										outJSVar.deleteCharAt(outJSVar.length()-2);
									}
									outJSVar.append(s);
									outJSVar.append("\n");
								}
								@Override
								public void onSubscribe(final Disposable d) {
									// nothin
								}
								@Override
								public void onComplete() {
									// nothing
								}
								@Override
								public void onError(final Throwable e) {
									// ignore
								}
				  	 });
		// WTF! dirty hack to remove extra "," at the last object's properties
		if (outJSVar.charAt(outJSVar.length()-2) == ',') {
			outJSVar.deleteCharAt(outJSVar.length()-2);
		}
		outJSVar.append(Strings.rightPad("",INITIAL_INDENT-1,'\t'))
				.append("}");
		return outJSVar.toString();
	}
	private static Map<String,Object> _extractPropertyMap(final String userAgentString,
														  final ReadableUserAgent userAgent) {
		Map<String,Object> props = Maps.newHashMap();
		
		if (Strings.isNOTNullOrEmpty(userAgentString)) props.put("header",_enquote(userAgentString));
		
		if (userAgent != null) {
			if (Strings.isNOTNullOrEmpty(userAgent.getName())) props.put("name",_enquote(userAgent.getName()));
			
			if (Strings.isNOTNullOrEmpty(userAgent.getProducer())) props.put("producer",_enquote(userAgent.getProducer()));
			
			if (userAgent.getFamily() != null || userAgent.getType() != null || userAgent.getDeviceCategory() != null) {
				Map<String,String> typoProps = Maps.newHashMapWithExpectedSize(3);
				if (userAgent.getFamily() != null) typoProps.put("family",_enquote(userAgent.getFamily().name()));
				if (userAgent.getType() != null) typoProps.put("type",_enquote(userAgent.getType().name()));
				if (userAgent.getDeviceCategory() != null) typoProps.put("category",_enquote(userAgent.getDeviceCategory().getCategory().name()));
				props.put("typo",typoProps);
			}
			
			if (userAgent.getVersionNumber() != null) props.put("version",_versionNumberProps(userAgent.getVersionNumber()));
			
			if (userAgent.getOperatingSystem() != null) {
				Map<String,Object> osProps = Maps.newHashMapWithExpectedSize(3);
				if (userAgent.getOperatingSystem().getFamily() != null) osProps.put("family",_enquote(userAgent.getOperatingSystem().getFamily().name()));
				if (userAgent.getOperatingSystem().getProducer() != null) osProps.put("producer",_enquote(userAgent.getOperatingSystem().getProducer()));
				if (userAgent.getOperatingSystem().getVersionNumber() != null) {
					osProps.put("version",_versionNumberProps(userAgent.getOperatingSystem().getVersionNumber()));
				}
				props.put("os",osProps);
			}
		} else {
			props = Maps.newHashMap();
		}
		return props;
	}
	private static Map<String,String> _versionNumberProps(final VersionNumber version) {
		Map<String,String> outVerProps = Maps.newHashMap();
		if (Strings.isNOTNullOrEmpty(version.toVersionString())) outVerProps.put("num",_enquote(version.toVersionString()));
		if (Strings.isNOTNullOrEmpty(version.getMajor())) outVerProps.put("major",_enquote(version.getMajor()));
		if (Strings.isNOTNullOrEmpty(version.getMinor())) outVerProps.put("minor",_enquote(version.getMinor()));
		if (Strings.isNOTNullOrEmpty(version.getExtension())) outVerProps.put("extension",_enquote(version.getExtension()));
		if (Strings.isNOTNullOrEmpty(version.getBugfix())) outVerProps.put("bugFix",_enquote(version.getBugfix()));	
		return outVerProps;
	}
	private static String _enquote(final String str) {
		return "'" + StringEscapeUtils.escapeEcmaScript(str) + "'";
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Transforms the properties map into an observable of js vars
	 * It iterates through the map entries and:
	 * 		- If the entry contains a simple property, returns propKey : propValue
	 * 		- If the entry contains a complex property (an object), returns propKey : { -the prop value js- }
	 * @param indent
	 * @param props
	 * @return
	 */
	private static Observable<String> _observableFrom(final int indent,
													  final Map<String,Object> props) {
		return Observable.fromIterable(props.entrySet())
					     .flatMap(new Function<Map.Entry<String,Object>,Observable<String>>() {
											@Override @SuppressWarnings("unchecked")
											public Observable<String> apply(final Entry<String,Object> me) {
												Observable<String> outObs = null;
																							
												if (me.getValue() instanceof String) {
													// simple property
													outObs = Observable.just(new StringBuilder()
																					.append(Strings.rightPad("",indent,'\t'))
																					.append(me.getKey())
																					.append(" : ")
																					.append((String)me.getValue())
																					.append(",")						// WTF! creates an extra , if it's the object's last property; it's removed when consumed
																					.toString());
												} 
												else if (me.getValue() instanceof Map) {
													// complex property (an object with properties) --> recurse!!
													Map<String,Object> subProps = (Map<String,Object>)me.getValue();
													if (CollectionUtils.hasData(subProps)) {
														outObs = Observable.merge(Observable.just(new StringBuilder()
																											.append(Strings.rightPad("",indent,'\t'))
																											.append(me.getKey())
																											.append(" : {")
																											.toString()),
																				  // Recurse!!!
																				  _observableFrom(indent+1,
																								  subProps),		
																				  Observable.just(new StringBuilder()
																						  				.append(Strings.rightPad("",indent,'\t'))
																						  				.append("}")
																						  				.append(",")	// WTF! creates an extra , if it's the object's last property; it's removed when consumed
																						  				.toString()));
													} else {
														outObs = Observable.empty();
													}
												}
												return outObs;
											}
					  		   	  });
	}
}
