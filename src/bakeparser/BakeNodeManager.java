package bakeparser;

import java.util.Hashtable;

public class BakeNodeManager {
	
	
	private Hashtable<String,BakeNode> directory;
	
	public BakeNodeManager(){
		directory = new Hashtable<String,BakeNode>();
	}
	
	
	private static class BakeNode
	{
		private Hashtable<String,BakeNode> directory;
		private String startTagMethodName,contentTagMethodName,attributesMethodName,endTagMethodName,objectGetterMethodName,tagName;
		private Object callObject;
		public BakeNode(String reversePath, Object callObject, String objectGetterMethodName,String startTagMethodName,String contentTagMethodName, String attributesMethodName, String endTagMethodName)
		{
			directory = new Hashtable<String,BakeNode>();
			
			String spl[] = reversePath.split(">");
			tagName = spl[0];
			if(spl.length>1)
			{
				directory.put(spl[1], new BakeNode(reversePath.replace(tagName+">", ""),callObject, objectGetterMethodName, startTagMethodName, contentTagMethodName, attributesMethodName,endTagMethodName));
			}
			else
			{
				/*
				 * Assign Method Names
				 */
				this.callObject = callObject;
				this.objectGetterMethodName = objectGetterMethodName;
				this.startTagMethodName = startTagMethodName;
				this.contentTagMethodName = contentTagMethodName;
				this.attributesMethodName = attributesMethodName;
				this.endTagMethodName = endTagMethodName;
			}
			
		}
		
		public BakeNode searchBakeNode(String path)
		{
			String expl[] = path.split(">");
			if(expl[0].equals(tagName))
			if(expl.length>1)
			{
				BakeNode bk = directory.get(expl[1]);
				if(bk!=null)
				{
					return bk.searchBakeNode(path.replace(expl[0]+">", ""));
				}
			}
			else 
			{
				return this;
			}
			
			return null;
		}
		
		
		
		
	}
	
	

}
