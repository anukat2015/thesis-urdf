package util
{
	
	import mx.collections.ArrayCollection;
	
	/** This class represents a set interface. */
	public interface USet 
	{
		/** Removes all key/value pair mappings from this hashset. */
        function clear():void 
        
        /** 
         * Delivers the object (stored in memory) belonging to the given entry. 
         * 
         * @param entry the entry whose object (object reference) should be returned.
         * @return the object reference for the given entry. */
        function getEntry(entry:Object):Object 

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
        function add(entry:Object):Boolean 
        
        /**
         * Adds all entries of the given hashset to this hashset.
         * 
         * @param otherSet the specified hashset whose entires should be added.
         * */
        function addAll(otherSet:USet):void 

        /**
		 * Removes the entry from the hashset.
		 * 
		 * @param entry the entry that should be removed from the hashset.
		 * @return the entry, if the entry could be removed and null otherwise.
		 * */
        function remove(entry:Object):Object 
        
        /**
         * Removes all entries of the given hashset from this hashset.
         * 
         * @param otherSet the specified hashset whose entires should be removed from this hashset.
         * */
        function removeAll(otherSet:USet):void 
        
        /** 
		 * Checks if the specified entry is already in the hashset. 
		 * 
		 * @param entry the entry that should be checked for existence in the hashset.
		 * @return true, if the hashset contains the given entry and false otherwise.
		 * */
        function contains(entry:Object):Boolean 
        
        /** 
		 * Checks if this hashset contains all entries of the specified hashset. 
		 * 
		 * @param otherSet the other hashset whose entries should be contained in this hashset.
		 * @return true, if this hashset contains all entries of the specified hashset and false otherwise.
		 * */
        function containsAll(otherSet:USet):Boolean 
          
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
        function size():int 

        /**
         * Delivers a string object consisting of the internally stored entries.
         * 
         * @return the string representation of the hashset.
         * */
        function toString():String 

        /**
         * Delivers an array-collection representation of the internally stored entries.
         * 
         * @return the array-collection representation.
         * */
        function values():ArrayCollection 
        
        /**
         * Checks whether this hashset is empty or not.
         * 
         * @return true, if this hashset is empty and false otherwise.
         * */
        function isEmpty():Boolean 
        
        /** 
         * Delivers an object (set) that stores all the entries of the hashmap. 
         * 
         * @return the keys of the hashmap stored in a set.
         * */
       // public function entrySet():Object 
        
        /**
         * Checks whether both hashmaps (this instance and the specified hashmap) contain the same 
         * entries or not. 
         * 
         * @param otherSet the specified hashmap which should be tested for equality.
         * @return true, if both maps are equal and false otherwise.
         * */
        function equals(otherSet:USet):Boolean  
        
        /**
         * Delivers an array representation of the internally stored entries.
         * 
         * @return the array representation.
         * */
        function toArray():Array
	}
}