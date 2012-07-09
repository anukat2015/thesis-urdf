package util
{
	import mx.collections.ArrayCollection;
	
	import urdf.api.UObject;

    /**
     * This class represents a hashset implementation which is based on the ArrayCollection super class and uses 
     * UObject instances as keys.
     * <p>
     * This data structure stores the given UObject instances (entries) like keys. Methods roughly mirror 
     * those of the java HashSet class. 
     * 
     * @author Timm Meiser
     * @since 16.02.10
     * @version 1.0
     * 
     */ 
	public class UObjectHashSet //implements USet
	{
		
		//private var entries:ArrayCollection;
		/** The entries of the hashset. */
		private var entries:Object;
		
		/** The empty default constructor for the UHashSet object. */
		public function UObjectHashSet() {
			entries = new Object();
		}
		
		public function getEntries():Object {
			return entries;
		}
		
		public function setEntries(entries:Object):void {
			this.entries = entries;
		}
	    
	    /** Removes all key/value pair mappings from this hashset. */	
		public function clear():void {
			entries = new Object();
			//for (var key:String in entries)
              //  entries[key] = null;
		}
		
		/** 
         * Delivers the object (stored in memory) belonging to the given entry. 
         * 
         * @param entry the entry whose object (object reference) should be returned.
         * @return the object reference for the given entry. 
         * */
		public function getEntry(entry:UObject):UObject {
			//if(entries[entry.getId().toString()]) {
			var key:String = entry.getId().toString();
			
			return entries[key]; // otherwise nul is returned 
				//return entries[entry];
			//}
			
			/*var i:int = getItemIndex(entry);
            if (i>=0)
                return super.getItemAt(i);
            */
			//return null;
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
		public function add(entry:UObject):Boolean {
			//if(entries.contains(entry))
			/*if(entries[entry.getId().toString()] != null)
			   return false;
			
			entries.addItem(entry);
            return true;
            */
            var key:String = entry.getId().toString();
            
			if(entries[key] != undefined) {
			   entries[key] = entry;
			   return true;
			}
			else {
			   entries[key] = entry;
			   return false;
			}
		}
		
		/**
         * Adds all entries of the given hashset to this hashset.
         * 
         * @param otherSet the specified hashset whose entires should be added.
         * */
		public function addAll(otherSet:UObjectHashSet):void {
			for each (var entry:UObject in otherSet.getEntries())
			   this.add(entry);
		}
		
		/**
		 * Removes the entry from the hashset.
		 * 
		 * @param entry the entry that should be removed from the hashset.
		 * @return the entry, if the entry could be removed and null otherwise.
		 * */
		public function remove(entry:UObject):UObject {
			
            var key:String = entry.getId().toString();
            
            if(entries[key] != undefined) {
				var value:UObject = entries[key];
				entries[key] = undefined;
				return value;
			}
			else
			   return null;

		}
		
		/**
         * Removes all entries of the given hashset from this hashset.
         * 
         * @param otherSet the specified hashset whose entires should be removed from this hashset.
         * */
		public function removeAll(otherSet:UObjectHashSet):void {
			for each (var entry:UObject in otherSet.getEntries())
			   this.remove(entry);
		}
		
		/** 
		 * Checks if the specified entry is already in the hashset. 
		 * 
		 * @param entry the entry that should be checked for existence in the hashset.
		 * @return true, if the hashset contains the given entry and false otherwise.
		 * */
		public function contains(entry:UObject):Boolean
		{
			// key was already in the hashmap, so return true as indication.
            var key:String = entry.getId().toString();
            
			if(entries[key] != undefined) 
			   return true;
			else
			   return false;
		}
		
		/** 
		 * Checks if this hashset contains all entries of the specified hashset. 
		 * 
		 * @param otherSet the other hashset whose entries should be contained in this hashset.
		 * @return true, if this hashset contains all entries of the specified hashset and false otherwise.
		 * */
		public function containsAll(otherSet:UObjectHashSet):Boolean {
			for each (var entry:UObject in otherSet.getEntries()) {
				if(!entries.contains(entry))
				   return false;
			}
			return true;
		}
		
		/**
         * Delivers the size of (number of entries in) the hashset. 
         * 
         * @return the number of entries.
         * */
		public function size():int {
			var counter:int = 0;
			for each (var obj:Object in entries) {
				counter++;
			}
			return counter;
		}
		
		/**
         * Delivers a string object consisting of the internally stored entries.
         * 
         * @return the string representation of the hashset.
         * */
		public function toString():String
		{
			var printer:String = " ObjectHashMap is [";
        	
        	for (var key:String in entries) {
        	   printer += " key:" + key + ",value:" + entries[key] + " | ";
        	}
        	
        	printer += "]" + " and size is " + size();
        	
        	return printer;
		}
		
		/**
         * Delivers an array representation of the internally stored entries.
         * 
         * @return the array(native) representation.
         * */
		public function valuesNative():Array {
			var valueArray:Array = new Array();
            //for (var key:String in entries)
              //  valueArray.push(entries[key]);
            for each (var entry:UObject in entries)
                valueArray.push(entry);
            return valueArray;
		}
		
		/**
         * Delivers an array-collection representation of the internally stored entries.
         * 
         * @return the array-collection representation.
         * */
		public function values():ArrayCollection {
			var valueCollection:ArrayCollection = new ArrayCollection();
            for each (var entry:UObject in entries)
                valueCollection.addItem(entry);
            return valueCollection;
		}
		
		/**
         * Checks whether this hashset is empty or not.
         * 
         * @return true, if this hashset is empty and false otherwise.
         * */
		public function isEmpty():Boolean {
            return (size() == 0) ? true : false;
		}
		
		/**
         * Checks whether both hashmaps (this instance and the specified hashmap) contain the same 
         * entries or not. 
         * 
         * @param otherSet the specified hashmap which should be tested for equality.
         * @return true, if both maps are equal and false otherwise.
         * */
		public function equals(otherSet:UObjectHashSet):Boolean {
			if(size() != otherSet.size())
               return false;
        
            for each (var otherEntry:UObject in otherSet.getEntries()) {
               if(!this.contains(otherEntry))
                  return false;
               //var entry:UObject = entries[key];
               //if(entry != otherSet.getEntry(entry))
                 // return false;
            }
            
            return true;
		}
		
		/**
         * Delivers an array representation of the internally stored entries.
         * 
         * @return the array representation.
         * */
        /*public function toArray():Array {
        	return entries.toArray();
        }*/
        
        /**
         * Fills the hashset with the entries from the given ArrayCollection object.
         * <p>
         * The entries have to be of type UObject.
         * 
         * @param entryCollection the collection of UObject entries. 
         * */
       /* public function fillHashSet(entryArray:Array):void {
           //for each (var entry:UObject in entryArray)
           var num:int = entryArray.length;
           //for (var i:int = 0; i<num; i++) {//var entry:UObject in entryArray.)
           while(entryArray.length > 0) {
              var entry:UObject = (UObject)(entryArray.pop());
              this.add(entry);
           }
        }*/
        
        /**
         * Fills the hashset with the entries from the given ArrayCollection object.
         * <p>
         * The entries have to be of type UObject.
         * 
         * @param entryCollection the collection of UObject entries. 
         * */
        public function fillHashSet(entryCollection:ArrayCollection):void {
           for each (var entry:UObject in entryCollection)
              this.add(entry);
        }
        
        /**
         * Fills the hashset with the entries from the given Array(native) object.
         * <p>
         * The entries have to be of type UObject.
         * 
         * @param entryArray the array of UObject entries. 
         * */
        public function fillHashSetNative(entryArray:Array):void {
           for each (var entry:UObject in entryArray)
              this.add(entry);
        }
        
        /**
         * Fills the hashset with the entries from the given Object instance.
         * <p>
         * The entries have to be of type UObject.
         * 
         * @param entryObject the collection of UObject entries. 
         * */
        public function fillHashSetFO(entryObject:Object):void {
           for each (var entry:UObject in entryObject)
              this.add(entry);
        }
		
		
	}
}