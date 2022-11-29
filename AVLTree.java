//Jack Newman 
package p5;
import java.io.*;
import java.util.*; 

public class AVLTree {
	
	/*Implements a ALV tree of ints (the keys) and other data (fixed length character strings and ints) stored in a random access file.
	Duplicates keys are not allowed. There will be at least 1 character string field*/
	
	private RandomAccessFile f;
	private long root; //the address of the root node in the file
	private long free; //the address in the file of the first node in the free list
	private int numStringFields; //the number of fixed length character fields
	private int fieldLengths[]; //the length of each character field
	private int numIntFields; // the number of integer fields
	

	
	
	//creates a new empty AVL tree stored in the file fname the number of character string fields is stringFieldLengths.length
	//stringFieldLengths contains the length of each string field

	public AVLTree(String fname, int stringFieldLengths[], int numIntFields) throws IOException {  
		File path = new File(fname); // may need ".txt" 
		 if(path.exists()) {path.delete(); } 
		  
		 
		 f = new RandomAccessFile(path, "rw"); //"Rw" is due to file arugment path. 
		 root = 0; //Setting global varible 
		 free = 0; 
		 this.numStringFields = stringFieldLengths.length;   
		 fieldLengths = new int[numStringFields]; //Im setting the size of Fieldlengths
		 this.numIntFields = numIntFields; 
		
		 f.writeLong(0); //Inserting root Its less work just to insert zero, than call global varbibles 
		 f.writeLong(0); //Inserting 
		 f.writeInt(numStringFields);
		 for(int i = 0; i < numStringFields; i++) {
			 fieldLengths[i] = stringFieldLengths[i]; //I refill global varible
			 f.writeInt(fieldLengths[i]); //I write each field length 
		 } 
		 f.writeInt(numIntFields);
	} 

		public AVLTree(String fname) throws IOException { //reuse an existing tree stored in the file fname 
			File path = new File(fname); //doesnt nessacrily make a new file.
			f =  new RandomAccessFile(path, "rw");
			f.seek(0);
			root = f.readLong(); 
			free = f.readLong(); 
			numStringFields = f.readInt(); 
			fieldLengths = new int[numStringFields]; 
			for(int i = 0; i < numStringFields; i ++) { fieldLengths[i] = f.readInt(); } 
			numIntFields = f.readInt(); 
		}   
	
	
		
		
		//PRE: the number and lengths of the sFields and iFields match the expected number and lengths
		//insert k and the fields into the tree
		//the string fields are null (‘\0’) padded if k is in the tree do nothing
		
		
		public void insert(int k, char sFields[][], int iFields[]) throws IOException { root = Insert(root,k, sFields, iFields); }
		
		//This method is for simply inserting into the tree and then calling apon the balance method if it does. It checks for duplication. 
		 private long Insert(long adress, int k, char sFields[][], int iFields[]) throws IOException { 
			 Node temp; //Makes temp node. 
		 	 if (adress== 0) {  //Base case. If Root, or the spot you recruse into is 0. //Will never have to balance here cause this is always a leaf 
		 	 	 temp = new Node(0, k, 0, sFields, iFields); //makes a node. 
		 	 	 long addr = getFree(); //Get me either the first spot in freelist, or next spot in memory. 
		 	 	 temp.writeNode(addr); //I write it in. 
		 	 	 return addr; //Then I return the spot 
		 	 }
		 	 temp = new Node(adress); //Else make a node to read the current data. 
		 	 if (k < temp.key) { temp.left = Insert(temp.left, k, sFields, iFields ); } //I read the nodes data. If k is smaller then I recurse left. 
		 	 else if (k > temp.key) { temp.right = Insert(temp.right, k, sFields, iFields ); } //If k is bigger. I recurse right. 
		 	 else { return adress; } //Else its a duplicate. 
		 	 int L = temp.left != 0? height(temp.left): -1; // All I need here is heights. So I use my special constructor. 
			 int R = temp.right != 0? height(temp.right): -1; //Because a node base height is 0. I have to check if it has children. I check its children hieghts 
			 temp.height = Math.max( L, R) +1; //Then I compare it. 
		 	 temp.writeNode(adress); //I write the node in at the adress 
		 	 adress = balance(adress); //I balance it. I only need to balance the parent. Never a leaf, because a leaf is handled in a base case.  
		 	 return adress; 
		 }
		 
		
		 //this is my balancing method
		 private long balance(long adress) throws IOException
		 { 
			if(adress == 0) { return 0; } //This a special case for my delate method. 
			Node subTree = new Node(adress); 
			Node l = new Node(subTree.left, true); //This is a special contsructor for run time. That I only gets me left, right, and hieght.  
			Node r = new Node(subTree.right, true);
			
			if(Math.abs(l.height - r.height) <= 1) { return adress; } // They are balanced, nothing needed to be done. By doing Absoulute value, i get to check both ways, and if they are equal.  
			if(l.height - r.height == 2) //1 is the allowed difference. If left is bigger or at one. Then it isnt a problem.  
			{
				int lL = l.left != 0? height(l.left): -1; // All I need here is heights. So I use my special constructor. 
				int lR = l.right != 0? height(l.right): -1; 
				if( lL >= lR) { adress = rotateSingleLeft(adress); } 
				else { adress = doubleLeftRight(adress); } 
			}
			else //Else you check the right side. 
			{ 
				if(r.height - l.height == 2) {
					int rR = r.right != 0? height(r.right): -1; 
					int rL = r.left != 0? height(r.left): -1; 
					if( rR >= rL) { adress = rotateSingleRight(adress); } //
					else { adress = doubleRightLeft(adress); } 
				} 
			}
			
		 subTree = new Node(adress); 
		 int L = subTree.left != 0? height(subTree.left): -1; // All I need here is heights. So I use my special constructor. 
		 int R = subTree.right != 0? height(subTree.right): -1; 
		 subTree.height = Math.max( L, R) +1; 
		 subTree.writeNode(adress);
		 return adress; 
		 }
		 
		
		 //My private method to do left single rotations. 
		 private long rotateSingleLeft(long adress) throws IOException{ 	 
			 Node top = new Node(adress); //The adress will any root of the subtree 
			 Node middle = new Node(top.left); //In a single rotation. The middle of the imbalance becomes the new root. 
			 long newRoot = top.left; //I need to rewrite these nodes heights at the end. So I need to save this adress
			 top.left = middle.right; //When I rotate the tree, m's right will fall be tops right. I have to do this first. 
			 middle.right = adress; //Adress stands for old root. 
			 int topChildL = top.left!= 0?height(top.left): -1;  // I use my special method that cuts down on run time, and only finds hieght. It will also check if adress is zero, thus skipping reading the list.  
			 int topChildR = top.right!=0?height(top.right): -1; //If the the spot doesnt exist in memory. Then It just returns a zero. If i were to check it indepdently. That would be extra ressasigment. Which I could skip.
			 int mChildL = middle.left != 0? height(middle.left): -1;  
			 top.height = Math.max(topChildL, topChildR) + 1; //I take whatever is higher between the two, and update tops spot 
			 middle.height = Math.max(mChildL, top.height) + 1; 
			 top.writeNode(adress);  //Rewriting the nodes. 
			 middle.writeNode(newRoot);
			 return newRoot; 
		 }
		 
		 //my private Method to do right single roations. 
		 private long rotateSingleRight(long adress) throws IOException { //LR or rl. 
				Node top = new Node(adress); //grabs adress of the root i have to change 
				Node middle = new Node(top.right); //grabs the right, which I have to change stuff around 
				long newRoot = top.right; //saves the adress of the right. 
				top.right = middle.left; //I adjust the tops right which was orginally middle to its right 
				middle.left = adress; //I set its right to the adress. Thus putting it on top 
				int topChildL = top.left!= 0? height(top.left): -1; //I get the topchild left and rights hieght 
				int topChildR = top.right!= 0? height(top.right): -1;  
				int mChildR =  middle.right != 0? height(middle.right): -1;  
				top.height = Math.max(topChildL, topChildR)+ 1; //i add because it inlcudes the biggest children + the parent height. or just one bigger than the children.  
				middle.height = Math.max(top.height, mChildR) + 1; 
				top.writeNode(adress);
				middle.writeNode(newRoot);
				return newRoot; 
			}
		 
		 
		 //Private method to do double roattion of the left and right kind.
		 private long doubleLeftRight(long adress) throws IOException {
			 	Node orginalRoot = new Node(adress);  
			 	orginalRoot.left = rotateSingleRight(orginalRoot.left); //A double Left right. Will first require a single right rotation on its left side. Which will then set it up for a single left rotation
			 	orginalRoot.writeNode(adress); //After rotating the right. I have to update the orginalRoot. So when I call on it again. It values will be updated correctly. 
			 	return rotateSingleLeft(adress); //Then I do the single left rotation here. Which changes the postion of the orginalRoot(which isn't important anymore) and updates everything within the tree. Then returns the new root. 
			}
		 
		 
		//Private method to do a double roation of the right and left kind 
		private long doubleRightLeft(long adress) throws IOException {
			Node orginalRoot = new Node(adress); 
			orginalRoot.right = rotateSingleLeft(orginalRoot.right); //A double right left. Will first require a single left rotation on its right side. Which will set it up for a single right rotation. 
			orginalRoot.writeNode(adress); //After rotating the left. I have to update the orginal root. So when I call it again. its values will be updated correctly. 
			return rotateSingleRight(adress); //Then I do a single right Rotation here. Which changes the  postion of the  orginalRoot, and updates everything and then reuturns me the adress.   
		}

		//This gets either the first spot in the free list if it exists. Otherwise it get the next spot in memory.  
		private long getFree() throws IOException { 
			long adress; 
			if(free != 0){  //If the freelist has a spot. I want to take that spot
				adress = free; //I save the first spot in the freeList. 
				f.seek(free); //I seek spot the freelist has saved. 
				free = f.readInt(); //Now I read the key value of the first thing in the freelist. Which represents either the end of the freelist, or the next spot. 
				f.seek(0); //I start back up to the top of the file. 
				f.readLong(); //I read the root. Which then sets the pointer to the next spot which is the freelist. 
				f.writeLong(free); //I write in 2nd value in the freelist as first spot in the freelist. Which could either be zero or another value. 
				return adress; //I return the first spot in the freelist. 
			} 
			return f.length(); //else I return the last spot in memory. 
		}
				 
		 
		 //I use this to cutDown on runtime. By Skipping varible Assigment. Its for the case in which I just need hieght. Its for the nodes I dont update. 
		 private int height(long addr) throws IOException{ 
			 f.seek(addr);
			 f.readInt(); 
			 for(int row = 0; row < numStringFields; row++){ for(int col = 0; col < fieldLengths[row]; col++){ f.readChar(); } } 
			 for(int row = 0; row < numIntFields; row++) {f.readInt();}
			 f.readLong(); 
			 f.readLong(); 
			 return f.readInt(); 
		 }
		 
		 
		 
		//if k is in the tree remove the node with key k from the tree otherwise do nothing
		 public void remove(int k) throws IOException {
			 	if(root == 0 ) {return; } //Nothing can happen
			 	root = remove(k, root); 
				 }
		 
		 //Private recursive method to do actaul work for remove
		 private long remove(int k, long addr) throws IOException { 
		     if(addr == 0) { return 0; }  //just a base case. 
			 Node temp = new Node(addr);  
			 
			 if(k < temp.key) { //Checking the left and rights. By only checking < > that means if doesnt exist, and hits a spot where a node has 0. Then it will just return back up.  
				 if(temp.left == 0) { return addr; } //Then your thing does not exist. 
				 temp.left = remove(k, temp.left); 
				 temp.writeNode(addr);
				}  
			 else if( k > temp.key) { 
				 if(temp.right == 0) { return addr; } 
				 temp.right = remove(k, temp.right); 
				 temp.writeNode(addr);
				 }  
			 else if( temp.left != 0 && temp.right != 0) //Then you know your at a spot in which does equal the value, and its left and rights exist
			 {
				 long newAdress = findMin(temp.right); //I find the adress that will replace the node im removing.  
				 Node replacement = new Node(newAdress); //I take the min value and its node data. 
				 replacement.left = temp.left; //The left adress will stay the same. 
				 replacement.right = remove(replacement.key, temp.right); //I take the value of the node im currenlty in, because it now exists as a duplicate. So I have remove that value now. 
				 replacement.writeNode(addr); //I take the old adress, and I write the new data in. 
			 }
			 else { //Case in which your at a node, that has 0-1 children. 
				 if(temp.left == 0 && temp.right == 0) {  //Case in which it is a child. 
					 addToFreeList(addr); //I add thing to free list. 
					 return 0; //Then I return the old adress as 0; 
				 }
				 long newAddress = temp.left != 0? temp.left: temp.right; //IF the left doesnt exist. Then the right MUST EXIST. If the left exists. Then the right doesnt exist. 
				 Node replacement = new Node(newAddress); //I write the data from the children node. 
				 replacement.writeNode(addr); //I rewrite over the old spot. 
				 addToFreeList(newAddress); //I add the children spot to the free list because we moved it up. THIS DOES NOT Do a cacasde of returns 
			 	} 
			 temp = new Node(addr); 
			 int L = temp.left != 0? height(temp.left): -1; // All I need here is heights. So I use my special constructor. 
			 int R = temp.right != 0? height(temp.right): -1; 
			 temp.height = Math.max( L, R) +1; 
			 temp.writeNode(addr);
			 return balance(addr); 
		 }
		 
		
		
		//This private method adds to the top of the freeList. 
		private void addToFreeList(long addr) throws IOException {
			f.seek(addr); //I seek the adress of thing im going to add to the first list.  
			f.writeInt( (int) free); // I add in the value in where the freeList is pointing to. Which could either be zero, or somewhere spot in memory. 
			f.seek(0); f.readLong(); //I go back up to the top of the list. Then read the root. Which then puts my file pointer at the start of free
			f.writeLong(addr); //Then I write the new top of the freelist. Which is the location of the adress. 	
			free = addr; 
		}
		
		//private method to find the min value of the right side. By finding the leftmost Node. if there is no leftMost node. Then I dont go down anyfurther. 
		private long findMin(long addr) throws IOException {
			long deeperAddr = addr; //A temp varible. That I use to decect if the value is 0.
			while(deeperAddr != 0) //A loop that will go untill the deeperNode is 0. 
			{ 
				f.seek(addr); //All methods bellow are just to read through a full Node. To get to the infomation I need. 
				f.readInt(); 
				for(int row = 0; row < numStringFields; row++){ for(int col = 0; col < fieldLengths[row]; col++){ f.readChar(); } } 
				for(int row = 0; row < numIntFields; row++) {f.readInt();}
				deeperAddr = f.readLong(); //Will read the first left. 
				if(deeperAddr != 0) { addr = deeperAddr; } //If the first left does not equal 0. Then I update adress. This is controll when I update adress. 
			}
			return addr; //Then I return the furthest left.
			
		}
		
		
		
		//if k is in the tree return a linked list of the strings fields associated with k otherwise return null. //The strings in the list must NOT include the padding (i.e the null chars)
		 public LinkedList<String> stringFind(int k) throws IOException { 
			 if(root == 0) {return null; }			 
			 return stringString(root, k); 
			 } 
			
		//Special constructor to search for this recursively. 
		private LinkedList<String> stringString(long addr, int k) throws IOException {
			Node temp = new Node(true, addr); //special constructor to only get key, left and right. 
			LinkedList<String> LL = new LinkedList<String>();
			if( k > temp.key ) {  //If k is is greater. Then move down right. Putting ifstatements within an ifstatments means less checking.  
				if( temp.right != 0) { return LL = stringString(temp.right, k); } //The right existing would occur more often than it not. So I check for it.  
				else { return null; } //else it is null. So that means that data DNE
			}
			else if( k < temp.key ) { 
				if(temp.left != 0) { return LL = stringString(temp.left, k);} 
				else { return null; } 
			}
			else //they are the same. 
			{ 
				temp = new Node(addr); //Make a new node that has all the data. 
				StringBuilder tempStr = new StringBuilder(20); //pick a decent size. So if were ever to double. It most likely would not have to double again.   
				for( int row = 0; row < numStringFields; row++){ 
					for(int col = 0; col < fieldLengths[row] && temp.stringFields[row][col] != '\0'; col++){ tempStr.append(temp.stringFields[row][col]); }//I append at each spot. 
					LL.add(tempStr.toString()); //Add current String. 
					tempStr.delete(0, tempStr.length()); //Delate off all current data in stringBuilder. 
				}
				return LL; 
			}
		}

		//if k is in the tree return a linked list of the integer fields associated with k otherwise return null
			
		public LinkedList<Integer> intFind(int k) throws IOException {
			 if(root == 0) {return null; }			 
			 return intFind(root, k); 
			 } 
		
		
		//Bascily the same logic and ideas from my LinkedList Integer idea. 
		private LinkedList<Integer> intFind(long addr, int k) throws IOException{ 
			Node temp = new Node(true, addr); //special constructor to only get key, left and right. 
			LinkedList<Integer> LL = new LinkedList<Integer>(); 
			if(k > temp.key) { //If k is is greater. Then move down right. Putting if inside statements within an ifstatments means less checking. ALso checking if its greater than or less than will occur more often than it being equal 
				if(temp.right != 0) { return LL = intFind(temp.right, k); } //The right existing would occur more often than it not. So I check for it. 
				return null ;  //else it is null. So that means that data DNE
			} 
			else if(k < temp.key) { 
				if(temp.left != 0) { return LL = intFind(temp.left, k);} 
				return null;  
			} 
			else 
			{ 
				temp = new Node(addr); 
				for(int row = 0; row < numIntFields; row++) { LL.add(temp.intFields[row]);} //Add each int
				return LL; 
			}
			
		}
	
		//Print the contents of the nodes in the tree is ascending order of the key print one node per line.
		 //Include the address of the node, the key, the character string fields (without padding), the int fields, the height and the child addresses
		
		public void print() throws IOException {
			if(root == 0) { return; } 
			print(root); 
			 
		}
		
		//My private recursive method that prints in InOrder
		private void print(long addr) throws IOException{ 
			Node temp = new Node(addr, true); //Special node constructor that just saves me runtime. 
			if( temp.left != 0) { print(temp.left); } //Checking if I even need to recurse into the nodes left side. I set recursion for the thing. 
			System.out.println(writeNodeString(addr)); 
			if( temp.right != 0) { print(temp.right); }			 
		}
		
		//To write my node out efficently 
		//FootNote: After further research and some self research. I found that new versions of java will append if its + or concectation is done in one string. However if you keep doing += that will make the runtimer slower. Concectation in one string uses string builder 
		//However making a loop. will create a stringBuilder Object everytime. thus slowing it down. So just create one stringBuilder object 
		
		private String writeNodeString(long addr) throws IOException { 
			Node temp = new Node(addr);  //Making a new node based off the adress. 
			StringBuilder finalString;
			int size = 0; //Keep track of total sizing of the Sfields and Ifields. 
			String adress = "Adress: "+ addr + " "; 
			String key = "Key: " + temp.key+ " "; 
			String left = "Left: " + temp.left+ " "; 
			String right = "Right: " + temp.right + " "; 
			String hieght = "Hieght: " + temp.height + " "; 
		
			StringBuilder allStr[] = new StringBuilder[numStringFields];  //Make a stringBuilder Array so I can make a specific amount of fixed length charcters. 
			StringBuilder allInts[] = new StringBuilder[numIntFields];	
			for( int row = 0; row < numStringFields; row++) { //Filling up each string, based of chars. 
				allStr[row] = new StringBuilder(fieldLengths[row]); //Gives a spefic size for the stringBuilder. So if it ever bigger than 16. It never has to copy the array. 
				for(int col = 0; col < fieldLengths[row] && temp.stringFields[row][col] != '\0'; col++){ allStr[row].append(temp.stringFields[row][col]); size++; }//I append at each spot, and upate size
				size++; //Keeping track of the total size, and add one for a extra space. 
				}
			
			for(int row = 0; row < numIntFields; row++) { 
				allInts[row] = new StringBuilder(); //Make an StringBuilder based off standerd size. I have a feeling it would save more runtime than doing temp.stringFields[row].toString().length()
				allInts[row].append(temp.intFields[row]); //I append the integer into a stringform
				size += allInts[row].length(); 
			}
			size+=18+14+11; //6 for each tab + * 2 extra spaces, and 6 spaces, 14 for StringFields, 11 for IntFields. 
		

			finalString = new StringBuilder( adress.length() + key.length() + left.length() + right.length() + hieght.length() + size ); 
			finalString.append(adress); 
			finalString.append(" \t "); 
			finalString.append(key);
		    finalString.append(" \t ");  
			finalString.append("StringFields: " );
			for(int row = 0; row < allStr.length; row++) { finalString.append(allStr[row]); }  
			finalString.append(" \t ");  
			finalString.append("IntFields: "); 
			for(int row = 0; row < allInts.length; row++) { finalString.append(allInts[row]);}
			finalString.append(" \t "); 
			finalString.append(left);
			finalString.append(" \t "); 
			finalString.append(right);
			finalString.append(" \t "); 
			finalString.append(hieght); 
			return finalString.toString(); //With all the data in order and everything I can now return the string. 
		}
		
		
		//Everytime I seek a free. I know that long is 7-8 
		//Print one line containing a comma delimited list of the addresses of nodes in the free list
		public void printFree() throws IOException {
			StringBuilder s = new StringBuilder(100); //I set it to decent size. Knowing that the freelist in most case will go up to or over 100.  But in most cases it wont. But im trying to avoid data copying 
			int curr = (int) free; 
			if(curr == 0 ) { return; } //No freeList 
			s.append("Freelist: "); 
			while( curr != 0) //The freelist has something. 
			{ 
				s.append(curr); s.append(", ");//I add in the spot. 
				f.seek(curr); //I seek the next spot. 
				curr = f.readInt(); 
			}
			s.delete( s.length()-2, s.length()-1); //Delate the extra comma. 
			System.out.print(s.toString());
			 }
		
		 //update root and free in the file (if necessary) close the random access file
		public void close() throws IOException { 
			f.seek(0); 
			f.writeLong(root); 
			f.writeLong(free);
			f.close(); }
			
			
			
		 
	
	private class Node {
		 private int key;
		 private char stringFields[][];
		 private int intFields[];
		 private long left;
		 private long right;
		 private int height;
		 private Node(long l, int d, long r, char sFields[][], int iFields[]) { //constructor for a new node v
			 key = d; 
			 stringFields = sFields; 
			 intFields = iFields; 
			 left = l; 
			 right = r; 
			 height = 0; 
		 }  
		 
		 private Node(long addr) throws IOException{ //constructor for a node that exists and is stored in the file 
			 f.seek(addr);
			 key = f.readInt(); 
			 stringFields = new char[fieldLengths.length][]; //Instainite how many rows there are. 
			 for(int row = 0; row < numStringFields; row++){ //Fill each row at a time. 
				 stringFields[row] = new char[fieldLengths[row]]; //I declare each row size, and then by calling fieldlengths at its correlating row. I instinate the the ragged arrays colloums size
				 for(int col = 0; col < fieldLengths[row]; col++){ //Moving across each specific collumn
					 stringFields[row][col] = f.readChar(); //Filling it up. 
				 } 
			 }
			 intFields = new int[numIntFields];
			 for(int row = 0; row < numIntFields; row++) {intFields[row] = f.readInt();} //Filling up the intfields based of each number of intfields 
			 left = f.readLong(); 
			 right = f.readLong(); 
			 height = f.readInt(); 
		 }  
		 
		 private void writeNode(long addr) throws IOException {  //writes the node to the file at location addr 	
			 f.seek(addr); 
			 f.writeInt(key);
			 for(int row = 0; row < numStringFields; row++){ //Fill each row at a time. 
				 for(int col = 0; col < fieldLengths[row]; col++){ //Moving across each specific collumn
					 f.writeChar(stringFields[row][col]);  //Writing in each char 
				 } 
			 } 
			 for(int row = 0; row < numIntFields; row++) { f.writeInt(intFields[row]); } //writing each int 
			 f.writeLong(left);
			 f.writeLong(right);
			 f.writeInt(height);
		 } 	
		 
		//I use this to cutDown on runtime. By Skipping varible Assigment. I just need left right and hieght. Boolean is there just to different the node constructor. 
		 private Node(long addr, boolean x) throws IOException{ 
			 if(addr == 0 ) { height = -1; return;} //If the adress I go into is zero. Then I must return a -1 hieght.  
			 f.seek(addr);
			 f.readInt(); 
			 for(int row = 0; row < numStringFields; row++){ for(int col = 0; col < fieldLengths[row]; col++){ f.readChar(); } } 
			 for(int row = 0; row < numIntFields; row++) {f.readInt();}
			 left = f.readLong(); 
			 right = f.readLong(); 
			 height = f.readInt(); 
		 }
		 private Node(boolean y, long addr) throws IOException{ //Just another private method. Just to get key, left and right. 
			 f.seek(addr);
			 key = f.readInt(); 
			 for(int row = 0; row < numStringFields; row++){ for(int col = 0; col < fieldLengths[row]; col++){ f.readChar(); } } 
			 for(int row = 0; row < numIntFields; row++) {f.readInt();}
			 left = f.readLong(); 
			 right = f.readLong(); 
		 }
				 
		 
	} 
	
	
	
	
	
}
