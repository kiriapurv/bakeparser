package bakeparser;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

public class BakeParserRequest
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
					e.printStackTrace();
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
									call = call.getClass().getMethod(methods[i].split(",")[0]).invoke(call);
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
		if(contentMethodName!=null)
		call(contentMethodName.replace("*", tagName),key,value);
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
