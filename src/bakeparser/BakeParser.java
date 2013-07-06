package bakeparser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import bakeparser.BakeParser.BakeParserRequestBuilder.BakeParserRequest;

public class BakeParser {
	
	/*
	 * @author Apurv Kiri
	 * 
	 * README
	 * - Bakeparser supports as needed parsing
	 * - Provide tag to parse as "parent>child1>child2" provide object and object's method which must have string argument
	 * - Parser will bake object for you.
	 */
	public static BakeParser newInstance()
	{
		return new BakeParser();
	}
	/*
	 * No public constructor
	 */
	private BakeParser(){}
	
	public static interface BakeParserListener
	{
		public BakeParserRequestBuilder buildRequests();
		public InputStream bakeParserStream();
		public void onBakingCompleted();
		public InputSource bakeParserSource();
	}
	
	public BakeParserRequestBuilder newRequestBuilder(String startTag)
	{
		return new BakeParserRequestBuilder(startTag);
	}
	public static class BakeParserRequestBuilder
	{
		/*
		 * Parent tag has to be mentioned.
		 */
		private String startTag;
		private LinkedList<BakeParserRequest> requests;
		public BakeParserRequestBuilder(String startTag)
		{
			this.startTag = startTag;
			requests = new LinkedList<BakeParserRequest>();
		}
		/*
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
		 */
		public BakeParserRequestBuilder addRequest(String tagName, Object callObject,String startMethodName, String contentMethodName,String parameterMethodName,String endTagMethod)
		{
			requests.add(new BakeParserRequest(tagName, callObject,startMethodName ,contentMethodName, parameterMethodName,endTagMethod));
			return this;
		}
		public BakeParserRequestBuilder addRequest(String tagName, Object callObject, String callObjectGetter,String startMethodName, String contentMethodName,String parameterMethodName,String endTagMethod)
		{
			requests.add(new BakeParserRequest(tagName, callObject, callObjectGetter ,startMethodName ,contentMethodName, parameterMethodName,endTagMethod));
			return this;
		}
		public void setStartTag(String startTag)
		{
			this.startTag = startTag;
		}
		
		public String getStartTag()
		{
			return startTag;
		}
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
			/*
			 * Useful getter setters
			 */
			public String getTagName()
			{
				return tagName;
			}
			
			/*
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
			
			/*
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							System.out.println("BakeParser > Method Not Found : "+callObject.getClass().getName()+" > "+methodName);
						} catch (NullPointerException e) {
							System.out.println("BakeParser > Null Pointer : "+callObject.getClass().getName()+" > "+methodName);
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
												System.out.println("BakeParser > Getter Returned Null Object : "+tempCall.getClass().getName()+" > "+methods[i]);
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							System.out.println("BakeParser > Method Not Found : "+call.getClass().getName()+" > "+methodName);
						} catch (NullPointerException e) {
							//System.out.println("BakeParser > Null Pointer : "+call.getClass().getName()+" > "+methodName);
							e.printStackTrace();
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
	public void registerListener(BakeParserListener listener) throws ParserConfigurationException, SAXException, IOException
	{
		Baker baker = new Baker(listener.buildRequests(),listener);
		
		SAXParserFactory fact = SAXParserFactory.newInstance();
		SAXParser parser = fact.newSAXParser();
		InputStream is = listener.bakeParserStream();
		if(is!=null)
		parser.parse(listener.bakeParserStream(), baker);
		else
		parser.parse(listener.bakeParserSource(), baker);
	}
	
		
		private static class Baker extends DefaultHandler
		{

			private Hashtable<String,BakeParserRequest> requests;
			private String currentTag="",startTag="",tempTag="";
			private BakeParserRequest currentRequest;
			private BakeParserListener listener;
			
			public Baker(BakeParserRequestBuilder requests, BakeParserListener listener)
			{
				startTag = requests.getStartTag();
				this.requests = new Hashtable<String,BakeParserRequest>();
				for(BakeParserRequest req : requests.getRequests())
				{
					this.requests.put(req.getTagName(),req );
				}
				
				this.listener = listener;
			}
			
			private String currentContent="";

			@Override
			public void startElement(String arg0, String localName, String qName,
					Attributes attrs) throws SAXException {
				tempTag = currentTag;
				
				if(qName.equals(startTag))
				{
					if(currentTag.equals(""))
					{
						currentTag=startTag;
					}
					else
					{
						currentTag+=">"+qName;
					}
				}
				else if(!currentTag.equals(""))
				{
					currentTag+=">"+qName;
				}
				
				currentRequest = requests.get(currentTag);
				if(currentRequest!=null)
				{
					currentRequest.callStartTagMethod();
					for(int i=0; i<attrs.getLength(); i++)
					{
						
						currentRequest.callParameterMethod(attrs.getQName(i), attrs.getValue(i));
					}	
				}
				else
				{
					currentRequest = requests.get(tempTag+">*");
					if(currentRequest!=null)
					{
						currentRequest.callStartTagMethod(qName);
						for(int i=0; i<attrs.getLength(); i++)
						{
							currentRequest.callParameterMethod(attrs.getQName(i), attrs.getValue(i),qName);
						}
					}
				}
				
			}
			@Override
			public void characters(char[] arg0, int arg1, int arg2)
					throws SAXException {
				if(currentRequest!=null)
				currentContent+=(new String(arg0).substring(arg1, arg1+arg2));
			}

			
			private String exp[];
			
			@Override
			public void endElement(String arg0, String localName, String qName)
					throws SAXException {
				
				
				
					currentRequest = requests.get(currentTag);
					/*
					 * Remove tag
					 */
					exp = null;
					exp = currentTag.split(">");
					if(exp.length!=0)
					{
						currentTag=exp[0];
						for(int i=1; i< exp.length-1; i++)
						{
							currentTag+=">"+exp[i];
							
						}
					}
					
						/*
						 * Calling for current tag
						 */
						if(currentRequest!=null)
						{
							if(!currentContent.trim().equals(""))
							currentRequest.callContentMethod(currentContent.trim());
							currentRequest.callEndTagMethod();
						}
						else
						{
							currentRequest = requests.get(currentTag+">*");
							if(currentRequest!=null)
							{
								if(!currentContent.trim().equals(""))
								currentRequest.callContentMethod(currentContent.trim(),qName);
								currentRequest.callEndTagMethod(qName);
							}
						}
						currentContent="";
						currentRequest = null;
						
						
				
			}
			
			@Override
			public void endDocument() throws SAXException {
				
				listener.onBakingCompleted();
				
			}	
			
		}
		
		
		
	
	

}
