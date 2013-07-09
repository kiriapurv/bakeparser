package bakeparser;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Hashtable;

public class BakeNodeManager {
	
	
	private Hashtable<String,BakeNode> directory;
	
	public BakeNodeManager(){
		directory = new Hashtable<String,BakeNode>();
	}
	
	public void addNode(String path,Object callObject,String objectGetterMethod, String startTagMethod, String contentMethod, String parameterMethod, String endTagMethod)
	{
		String s[] = path.split(">");
		if(directory.contains(s[s.length-1]))
			directory.get(s[s.length-1]).addNode(path, callObject,objectGetterMethod, startTagMethod, contentMethod, parameterMethod, endTagMethod);
		else
			directory.put(s[s.length-1],new BakeNode(path,callObject,objectGetterMethod,startTagMethod,contentMethod,parameterMethod,endTagMethod));
	}
	
	public BakeNode findNode(String path)
	{
		
		String s[] = path.split(">");

		
			if(directory.containsKey(s[s.length-1]))
			{
				
				return directory.get(s[s.length-1]).findNode(path);
			}
			else if(directory.containsKey("*"))
			{
				String p = s[0];
				for(int i=0; i<s.length-1;i++)
					p+=">"+s[i];
				p+=">*";
				return directory.get("*").findNode(p);
			}
			return null;
	}
	
	public static class BakeNode
	{
		//Child Nodes of BakeNode
		private Hashtable<String,BakeNode> childs;
		private String tagName,objectGetterMethod,startTagMethod,contentMethod,parameterMethod,endTagMethod;
		private Object callObject;
		private boolean objectGetterMode = false, parameterSplitMode=false;
		private Hashtable<String,String> parameterMethods;
		public BakeNode(String path,Object callObject,String objectGetterMethod,String startTagMethod,String contentMethod,String parameterMethod,String endTagMethod)
		{
			//Path is the thing which will identify and store function
			//The last element of the path is current node
			//if path is A>B>C>D ==> so this BakeNode is D
			
			childs = new Hashtable<String,BakeNode>();
			
			if(path.contains(">"))
			{
				String nm[] = path.split(">");
				this.tagName = nm[nm.length-1];
				String pa = nm[0];
				
				for(int i=1;i<nm.length-1;i++)
				{
					pa+=">"+nm[i];
				}
				childs.put(nm[nm.length-2],new BakeNode(pa,callObject,objectGetterMethod,startTagMethod,contentMethod,parameterMethod,endTagMethod));
				
			}
			else
			{
				this.tagName = path;
				this.objectGetterMethod = objectGetterMethod;
				if(objectGetterMethod!=null)
					objectGetterMode = true;
				this.startTagMethod = startTagMethod;
				this.contentMethod = contentMethod;
				this.parameterMethod = parameterMethod;
				this.endTagMethod = endTagMethod;
				this.callObject = callObject;
				setUpMethods();
				setUpParameters();
				
			}
		}
		
		private void setUpMethods()
		{
			if(!tagName.contains("*"))
			{
				if(contentMethod!=null)
				contentMethod = contentMethod.replace("*",tagName);
				if(endTagMethod!=null)
				endTagMethod = endTagMethod.replace("*", tagName);
				if(startTagMethod!=null)
				startTagMethod = startTagMethod.replace("*",tagName);
			}
		}
		private void setUpParameters()
		{
			if(parameterMethod!=null)
			{
				if(parameterMethod.contains(">"))
				{
					parameterMethods = new Hashtable<String,String>();
					parameterSplitMode = true;
					String deps[] = parameterMethod.split("\\|");
					
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
		
		public void addNode(String path,Object callObject, String objectGetterMethod, String startTagMethod, String contentMethod, String parameterMethod, String endTagMethod)
		{
			if(path.contains(">"))
			{
				String nm[] = path.split(">");
				if(tagName.equals(nm[nm.length-1]))
				{
					String pa = nm[0];
				
					for(int i=1;i<nm.length-1;i++)
					{
						pa+=">"+nm[i];
					}
					childs.put(nm[nm.length-2],new BakeNode(pa,callObject,objectGetterMethod,startTagMethod,contentMethod,parameterMethod,endTagMethod));
				}
			}
			
		}
		
		public BakeNode findNode(String path)
		{
			
			//The path is A>B>C>D
			
			if(path.contains(">"))
			{
				String nm[] = path.split(">");
				
				if(nm[nm.length-1].equals(tagName))
				{
					String pa = nm[0];
				
					for(int i=1;i<nm.length-1;i++)
					{
						pa+=">"+nm[i];
					}
					
					BakeNode node = childs.get(nm[nm.length-2]);
					if(node!=null)
						return node.findNode(pa);
					else
						return this;
				}
			}
			else
			{
				
				if(path.equals(tagName))
				{
					return this;
				}
				
			}
			return null;
		}
		
		/*
		 * Useful methods
		 */
		private void call(String methodName,Object... content)
		{
			if(methodName!=null && callObject!=null)
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
					String methods[] = objectGetterMethod.split(">");
					
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
			call(contentMethod,content);
		}
		
		public void callParameterMethod(String key, String value)
		{
			if(!parameterSplitMode)
			{
				call(parameterMethod,key,value);
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
			call(endTagMethod);
		}
		public void callStartTagMethod()
		{
			call(startTagMethod);
		}
		/*
		 * Methods for wildcards
		 */
		public void callContentMethod(String content, String tagName)
		{
			if(contentMethod!=null && content!=null)
			call(contentMethod.replace("*", tagName),content);
		}
		public void callParameterMethod(String key,String value, String tagName)
		{
			if(contentMethod!=null)
			call(contentMethod.replace("*", tagName),key,value);
		}
		public void callEndTagMethod(String tagName)
		{
			if(endTagMethod!=null)
			call(endTagMethod.replace("*", tagName));
		}
		public void callStartTagMethod(String tagName)
		{
			if(startTagMethod!=null)
			call(startTagMethod.replace("*", tagName));
		}
	
	}
	
	

}
