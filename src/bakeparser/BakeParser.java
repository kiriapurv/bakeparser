package bakeparser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Apurv Kiri
 * 
 * README
 * - BakeParser supports as needed parsing
 * - Provide tag to parse as "parent>child1>child2" provide object and object's method which must have string argument
 * - Parser will bake object for you.
 */
public class BakeParser {
	
	public static BakeParser newInstance(BakeParserRequestBuilder requestBuilder)
	{
		return new BakeParser(requestBuilder);
	}
	
	private BakeParserRequestBuilder mRequestBuilder;
	private BakeParserListener mListener;
	/*
	 * No public constructor
	 */
	private BakeParser(BakeParserRequestBuilder requestBuilder){
		mRequestBuilder = requestBuilder;
	}
	
	public static interface BakeParserListener
	{
		public void onBakingCompleted(String response);
	}
	
	public static BakeParserRequestBuilder newRequestBuilder(String startTag)
	{
		return new BakeParserRequestBuilder(startTag);
	}
	public static class BakeParserRequestBuilder
	{
		/**
		 * Parent tag has to be mentioned.
		 */
		private String startTag;
		private LinkedList<BakeParserRequest> requests;
		public BakeParserRequestBuilder(String startTag)
		{
			this.startTag = startTag;
			requests = new LinkedList<BakeParserRequest>();
		}
		/**
		 * When <tag> is started, callObject's startMethodName is called, for content contentMethodName is called, for parameter's of the tag, parameterMethodName is called
		 * for end of tag, endTagMethod is called.
		 * 
		 * ..................
		 * tagName has to be in format of parent>child1>child2>child3, skipping any of the hierarchy will not parse that tag.
		 * 
		 * startTag method has no parameters
		 * contentMethod has one String parameter
		 * parameter method has two string parameters as key,value
		 * endTag method has no parameters
		 * 
		 * @param tagName Tag path, on occurrence of which the methods will be invoked
		 * @param callObject Object of which the methods will be called
		 * @param startMethodName Called when tag is started
		 * @param contentMethodName Called when content is captured
		 * @param parameterMethodName Called when parameters are captured
		 * @param endTagMethod Called when tag is ended
		 * @return Current builder
		 * 
		 */
		public BakeParserRequestBuilder addRequest(String tagName, Object callObject,String startMethodName, String contentMethodName,String parameterMethodName,String endTagMethod)
		{
			requests.add(new BakeParserRequest(tagName, callObject,startMethodName ,contentMethodName, parameterMethodName,endTagMethod));
			return this;
		}
		/**
		 * 
		 * @param tagName Tag path, on occurrence of which the methods will be invoked
		 * @param callObject Object of which the methods will be called
		 * @param callObjectGetter In case when methods has to be invoked on the object returned by callObjectGetter parameter method name
		 * @param startMethodName Called when tag is started
		 * @param contentMethodName Called when content is captured
		 * @param parameterMethodName Called when parameters are captured
		 * @param endTagMethod Called when tag is ended
		 * @return Current builder
		 */
		public BakeParserRequestBuilder addRequest(String tagName, Object callObject, String callObjectGetter,String startMethodName, String contentMethodName,String parameterMethodName,String endTagMethod)
		{
			requests.add(new BakeParserRequest(tagName, callObject, callObjectGetter ,startMethodName ,contentMethodName, parameterMethodName,endTagMethod));
			return this;
		}
		/**
		 * Sets starting tag
		 * @param startTag name of starting tag
		 */
		public void setStartTag(String startTag)
		{
			this.startTag = startTag;
		}
		/**
		 * 
		 * @return Returns start tag
		 */
		public String getStartTag()
		{
			return startTag;
		}
		/**
		 * 
		 * @return List of requests ( used by BakeParser )
		 */
		public LinkedList<BakeParserRequest> getRequests()
		{
			return requests;
		}
		
		public static class BakeParserRequest
		{
			private String myTag,tagName,contentMethodName,parameterMethodName,endTagMethodName,startTagMethodName,objectGetter;
			private Object callObject;
			private boolean objectGetterMode = false, parameterSplitMode=false;
			private Hashtable<String,String> parameterMethods;
			public BakeParserRequest(String tagName, Object callObject,String startTagMethod,String contentMethodName,String parameterMethodName,String endTagMethod)
			{
				this(tagName,callObject,null,startTagMethod,contentMethodName,parameterMethodName,endTagMethod);
			}
			public BakeParserRequest(String tagName,Object callObject,String objectGetter,String startTagMethod,String contentMethodName,String parameterMethodName,String endTagMethod)
			{
				this.tagName = tagName;
				this.callObject = callObject;
				this.objectGetter = objectGetter;
				if(objectGetter!=null)
				objectGetterMode = true;
				this.contentMethodName = contentMethodName;
				this.parameterMethodName = parameterMethodName;
				this.endTagMethodName = endTagMethod;
				this.startTagMethodName = startTagMethod;
				String s[] = tagName.split(">");
				myTag = s[s.length-1];
				setUpMethods();
				setUpParameters();
			}
			private void setUpMethods()
			{
				if(!myTag.contains("*"))
				{
					if(contentMethodName!=null)
					contentMethodName = contentMethodName.replace("*",myTag);
					if(endTagMethodName!=null)
					endTagMethodName = endTagMethodName.replace("*", myTag);
					if(startTagMethodName!=null)
					startTagMethodName = startTagMethodName.replace("*",myTag);
				}
			}
			private void setUpParameters()
			{
				if(parameterMethodName!=null)
				{
					if(parameterMethodName.contains(">"))
					{
						parameterMethods = new Hashtable<String,String>();
						parameterSplitMode = true;
						String deps[] = parameterMethodName.split("\\|");
						
						for(String dep : deps)
						{
							String spl[] = dep.split(">");
							parameterMethods.put(spl[0],spl[1]);
						}
					}
					else
					{
						parameterSplitMode = false;
					}
				}
			}
			/**
			 * Useful getter setters
			 */
			public String getTagName()
			{
				return tagName;
			}
			
			/**
			 * Useless getter setters
			 */
			public String getContentMethodName()
			{
				return contentMethodName;
			}
			public String getParameterMethodName()
			{
				return parameterMethodName;
			}
			
			/**
			 * Useful methods
			 */
			private Object tempCall;
			private void call(String methodName,Object... content)
			{
				if(methodName!=null)
				{
					if(!objectGetterMode)
					{
						try {
							for(String mName : methodName.split(","))
							switch(content.length)
							{
								case 0 :
									callObject.getClass().getMethod(mName).invoke(callObject);
									break;
								case 1:
									callObject.getClass().getMethod(mName,String.class).invoke(callObject,content);
									break;
								case 2:
									callObject.getClass().getMethod(mName,String.class,String.class).invoke(callObject,content);
									break;
							}
							
							
						} catch (IllegalArgumentException e) {
							System.out.println("BakeParser > Invalid Argument being passed : "+callObject.getClass().getName()+" > "+methodName);
						} catch (SecurityException e) {
							System.out.println("BakeParser > Method Invokation not Permited : "+callObject.getClass().getName()+" > "+methodName);
						} catch (IllegalAccessException e) {
							System.out.println("BakeParser > Invalid Method Access Modifier : "+callObject.getClass().getName()+" > "+methodName);
						} catch (InvocationTargetException e) {
							System.out.println("BakeParser > Method Invokation Error : "+callObject.getClass().getName()+" > "+methodName);
						} catch (NoSuchMethodException e) {
							System.out.println("BakeParser > Method Not Found : "+callObject.getClass().getName()+" > "+methodName);
						} catch (NullPointerException e) {
							System.out.println("BakeParser > Null Pointer : "+((callObject!=null) ? callObject.getClass().getName() : "Null Object" )+" > "+methodName);
						}
						
					}
					else
					{
						String methods[] = objectGetter.split(">");
						
						Object call = callObject;
						try {
							for(String methodN : methodName.split(","))
							{
								
									int n=0;
									if(methodN.contains("<"))
									{
										char ch[] = methodN.toCharArray();
										for(char c : ch)
										{
											if(c=='<')
												n++;
											else
												break;
										}
										methodN = methodN.replace("<", "");
									}
									for(int i=0; i<methods.length-n;i++)
									{
											String s = methods[i].split(",")[0];
											tempCall = call;
											call = call.getClass().getMethod(s).invoke(call);
											if(call==null)
												System.out.println("BakeParser > Getter Returned Null Object : "+((tempCall!=null) ? tempCall.getClass().getName() : "Null Object")+" > "+methods[i]);
									}
									
									switch(content.length)
									{
										case 0 :
											call.getClass().getMethod(methodN).invoke(call);
											break;
										case 1:
											call.getClass().getMethod(methodN,String.class).invoke(call,content);
											break;
										case 2:
											call.getClass().getMethod(methodN,String.class,String.class).invoke(call,content);
											break;
									}
							}
								
						} catch (IllegalArgumentException e) {
							System.out.println("BakeParser > Invalid Argument being passed : "+call.getClass().getName()+" > "+methodName);
						} catch (SecurityException e) {
							System.out.println("BakeParser > Method Invokation not Permited : "+call.getClass().getName()+" > "+methodName);
						} catch (IllegalAccessException e) {
							System.out.println("BakeParser > Invalid Method Access Modifier : "+call.getClass().getName()+" > "+methodName);
						} catch (InvocationTargetException e) {
							System.out.println("BakeParser > Method Invokation Error : "+call.getClass().getName()+" > "+methodName);
						} catch (NoSuchMethodException e) {
							System.out.println("BakeParser > Method Not Found : "+call.getClass().getName()+" > "+methodName);
						} catch (NullPointerException e) {
							System.out.println("BakeParser > Null Pointer : "+((call!=null) ? call.getClass().getName() : "Null Object" )+" > "+methodName);
						}
					}
				}
			}
			public void callContentMethod(String content)
			{
				call(contentMethodName,content);
			}
			
			public void callParameterMethod(String key, String value)
			{
				if(!parameterSplitMode)
				{
					call(parameterMethodName,key,value);
				}
				else
				{
					String m = parameterMethods.get(key);
					
					if(m!=null)
					{
						if(m.contains("*"))
						{
							m = m.replace("*",key);
						}
						call(m,value);
					}
					else
					{
						m = parameterMethods.get("*");
						if(m!=null)
						{
							call(m,key,value);
						}
						
					}
				}
				
			}
			
			public void callEndTagMethod()
			{
				call(endTagMethodName);
			}
			public void callStartTagMethod()
			{
				call(startTagMethodName);
			}
			/*
			 * Methods for wildcards
			 */
			public void callContentMethod(String content, String tagName)
			{
				if(contentMethodName!=null && content!=null)
				call(contentMethodName.replace("*", tagName),content);
			}
			public void callParameterMethod(String key,String value, String tagName)
			{
				if(parameterMethodName!=null)
				call(parameterMethodName.replace("*", tagName),key,value);
			}
			public void callEndTagMethod(String tagName)
			{
				if(endTagMethodName!=null)
				call(endTagMethodName.replace("*", tagName));
			}
			public void callStartTagMethod(String tagName)
			{
				if(startTagMethodName!=null)
				call(startTagMethodName.replace("*", tagName));
			}
		}
		
	}
	/**
	 * Set listener which will be invoked for response xml content
	 * @param listener Listener instance
	 */
	public void setListener(BakeParserListener listener) {
		this.mListener = listener;
	}	
	/**
	 * Will parse the data of given InputSource and fill the objects provided in BakeParserRequestBuilder
	 * @param is InputSource of data
	 * @throws SAXException In case of invalid xml
	 * @throws IOException In case of io error
	 * @throws ParserConfigurationException In case of invalid configuration done by BakeParser for SAXParser
	 */
	public void parse(InputSource is) throws SAXException, IOException, ParserConfigurationException {
		
		Baker baker = new Baker(mRequestBuilder, mListener);
		
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxFactory.newSAXParser();
		saxParser.parse(is, baker);
		
	}
	/**
	 * Will parse the data of given InputStream and fill the objects provided in BakeParserRequestBuilder
	 * @param is InputStream of data
	 * @throws SAXException In case of invalid xml
	 * @throws IOException In case of io error
	 * @throws ParserConfigurationException In case of invalid configuration done by BakeParser for SAXParser
	 */
	public void parse(InputStream is) throws SAXException, IOException, ParserConfigurationException {
		parse(new InputSource(is));
	}

}
