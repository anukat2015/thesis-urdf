package util
{
	import mx.collections.ArrayCollection;

    /**
     * This class represents a hashset implementation which is based on the ArrayCollection super class and uses 
     * Object instances as keys.
     * <p>
     * This data structure stores the given Object instances (entries) like keys. Methods roughly mirror 
     * those of the java HashSet class. 
     * 
     * @author Timm Meiser
     * @since 16.02.10
     * @version 1.0
     * 
     */ 
	public class UObjectHashSetOld implements USet
	{
		
		private var entries:ArrayCollection;
		
		/** The empty default constructor for the UHashSet object. */
		public function UObjectHashSet(source:ArrayCollection=null) {
			//TODO: implement function
			//this.entries = source;
			if(source == null)
			   entries = new ArrayCollection();
			else
			   entries = source;
		}
		
		public function getEntries():ArrayCollection {
			return entries;
		}
		
		public function setEntries(entries:ArrayCollection):void {
			this.entries = entries;
		}
	    
	    /** Removes all key/value pair mappings from this hashset. */	
		public function clear():void {
			entries.removeAll();
			//return this;
		}
		
		/** 
         * Delivers the object (stored in memory) belonging to the given entry. 
         * 
         * @param entry the entry whose object (object reference) should be returned.
         * @return the object reference for the given entry. 
         * */
		public function getEntry(entry:Object):Object {
			if(entries.contains(entry)) {
				return entries[entry];
			}
			
			/*var i:int = getItemIndex(entry);
            if (i>=0)
                return super.getItemAt(i);
            */
			return null;
		}
		
		/**
         * Adds the given entry to the hashset.
         * <p>
         * It is checked if the given entry was already in the hashset. In this case, 
         * the method either overrides the object reference for the given entry or leaves 
         * the entry untouched (depends on how ActionScript compares objects internally, by values
         * contained in the object or the object references or both). If the given entry hasn`t 
         * existed so far, the method adds the entry, increases the <i> size-counter </i> and returns 
         * false.
         * 
         * @param entry the entry to add.
         * @return true, if the entry already existed and false otherwise.
         * */
		public function add(entry:Object):Boolean {
			if(entries.contains(entry))
			   return false;
			
			entries.addItem(entry);
            return true;

		}
		
		/**
         * Adds all entries of the given hashset to this hashset.
         * 
         * @param otherSet the specified hashset whose entires should be added.
         * */
		public function addAll(otherSet:USet):void {
			for(var entry:Object in otherSet)
			   this.add(entry);
		}
		
		/**
		 * Removes the entry from the hashset.
		 * 
		 * @param entry the entry that should be removed from the hashset.
		 * @return the entry, if the entry could be removed and null otherwise.
		 * */
		public function remove(entry:Object):Object {
			var position:int = entries.getItemIndex(entry);
            if (position >= 0) {  
                var obj:Object = entries.getItemAt(position); 
                entries.removeItemAt(position);
                return obj;
            }
            return null;

		}
		
		/**
         * Removes all entries of the given hashset from this hashset.
         * 
         * @param otherSet the specified hashset whose entires should be removed from this hashset.
         * */
		public function removeAll(otherSet:USet):void {
			for(var entry:Object in otherSet)
			   this.remove(entry);
		}
		
		/** 
		 * Checks if the specified entry is already in the hashset. 
		 * 
		 * @param entry the entry that should be checked for existence in the hashset.
		 * @return true, if the hashset contains the given entry and false otherwise.
		 * */
		public function contains(entry:Object):Boolean
		{
			return entries.contains(entry);
		}
		
		/** 
		 * Checks if this hashset contains all entries of the specified hashset. 
		 * 
		 * @param otherSet the other hashset whose entries should be contained in this hashset.
		 * @return true, if this hashset contains all entries of the specified hashset and false otherwise.
		 * */
		public function containsAll(otherSet:USet):Boolean {
			for(var entry:Object in otherSet) {
				if(!entries.contains(entry))
				   return false;
			}
			return true;
		}
		
		/**
         * Delivers the size of (number of entries in) the hashset.
         * <p>
         * It is necessary to compute the size at least once (though the methods
         * that add and remove entries adjust the size counter.) The size computation 
         * is necessary, because this object could be a serialized object which originates 
         * from a java hashset. In this case, we have no <i> size </i> data stored already and so
         * we will have to compute this data once. 
         * 
         * @return the number of entries.
         * */
		public function size():int {
			return entries.length;
		}
		
		/**
         * Delivers a string object consisting of the internally stored entries.
         * 
         * @return the string representation of the hashset.
         * */
		public function toString():String
		{
			return entries.toString();
		}
		
		/**
         * Delivers an array-collection representation of the internally stored entries.
         * 
         * @return the array-collection representation.
         * */
		public function values():ArrayCollection {
			return entries;
		}
		
		/**
         * Checks whether this hashset is empty or not.
         * 
         * @return true, if this hashset is empty and false otherwise.
         * */
		public function isEmpty():Boolean {
			return entries.length == 0;
		}
		
		/**
         * Checks whether both hashmaps (this instance and the specified hashmap) contain the same 
         * entries or not. 
         * 
         * @param otherSet the specified hashmap which should be tested for equality.
         * @return true, if both maps are equal and false otherwise.
         * */
		public function equals(otherSet:USet):Boolean {
			if(entries.length != otherSet.size())
			   return false;
			return this.containsAll(otherSet);
		}
		
		/**
         * Delivers an array representation of the internally stored entries.
         * 
         * @return the array representation.
         * */
        public function toArray():Array {
        	return entries.toArray();
        }
		
	}
}