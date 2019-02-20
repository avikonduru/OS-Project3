import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.List;
import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.lang.Object;
import java.util.Random;

public class Project3 {
	
	public static void main(String[] args) throws IOException{
			
		Logic logic = new Logic();
		Scanner sc = new Scanner(System.in);
		int allocationType;
		int choice;
		int input = 0;
		
		System.out.println("What file allocation method would you like:");
		System.out.println("1) Contiguous Allocation");
		System.out.println("2) Chained Allocation");
		System.out.println("3) Indexed Allocation");
		System.out.println();
		System.out.print("Choice: ");
		choice = sc.nextInt();
		System.out.println();
		
		if(choice == 2){
			allocationType = 2;
		}else if(choice == 3){
			allocationType = 3;
		}else{
			allocationType = 1;
		}
		
		while(input != 8){
			
			System.out.println("1) Display a file");
			System.out.println("2) Display the file table");
			System.out.println("3) Display the free space bitmap");
			System.out.println("4) Display a disk block");
			System.out.println("5) Copy a file from the simulation to a file on the real system");
			System.out.println("6) Copy a file from the real system to a file in the simulation");
			System.out.println("7) Delete a file");
			System.out.println("8) Exit");
			System.out.println();
			System.out.print("Choice: ");
			input = sc.nextInt();
			System.out.println();
			
			switch(input){
				case 1:
					Scanner fileIn = new Scanner(System.in);
					System.out.print("Enter file name: ");
					String fName = fileIn.nextLine();
					System.out.println();
					System.out.println(logic.displayFile(fName,allocationType));
					System.out.println();
					break;
				case 2:
					System.out.print(logic.getBlock(0));
					System.out.println();
					break;
				case 3:
					System.out.print(logic.getBlock(1));
					System.out.println();
					break;
				case 4:
					Scanner blockInput = new Scanner(System.in);
					System.out.print("Enter Block Number (0-255): ");
					int blockNum = blockInput.nextInt();
					System.out.println();
					System.out.println(logic.getBlock(blockNum));
					System.out.println();
					break;
				case 5:
					Scanner filInput = new Scanner(System.in);
					System.out.print("Copy from: ");
					String f1 = filInput.nextLine();
					System.out.print("Copy To: ");
					String f2 = filInput.nextLine();
					String contentFile = logic.displayFile(f1,allocationType);
					createNewFile(f2, contentFile);
					System.out.println();
					break;
				case 6:
					String file1, file2;
					File file;
					byte[] fileContent;
					Scanner fileInput = new Scanner(System.in);
					
					do{
						
						System.out.print("Copy From: ");
						file1 = fileInput.nextLine();
						file = new File(file1);
						fileContent = Files.readAllBytes(file.toPath());
						//System.out.println(fileContent.length);
						
					}while(!underCharLimit(file1) && underBlockLimit(fileContent.length));
					
					System.out.print("Copy To: ");
					file2 = fileInput.nextLine();
					System.out.println();
					logic.writeToBlocks(fileContent, allocationType, file2);
					    
					break;
				case 7:
					Scanner deleteIn = new Scanner(System.in);
					System.out.print("Enter File Name: ");
					String fiName = deleteIn.nextLine();
					System.out.println();
					logic.deleteFile(fiName, allocationType);
					System.out.println();
					System.out.println("File has been deleted");
					System.out.println();
					break;
				default:
					break;
			}
			
		}
		
		System.out.println("Program Ended");
			
		}

	public static boolean underBlockLimit(int fileLength){
		
		boolean underLimit = true;
		
		if((fileLength/512) > 10){
			
			underLimit = false;
			
		}
		
		return underLimit;
		
	}
	
	public static boolean underCharLimit(String file){
		
		boolean underLimit = true;
		int length = file.length();
		length = length - 4;
		
		if(length > 8){
			
			underLimit = false;
			
		}
		
		return underLimit;
		
	}
	
	public static void createNewFile(String filename, String content) throws IOException{
		
		File file = new File(filename);
		
		if (file.createNewFile())
		{
		    System.out.println("File is created!");
		} else {
		    System.out.println("File already exists.");
		}
		
		FileWriter writer = new FileWriter(file);
		writer.write(content);
		writer.close();
		
	}

}

class Logic{
	
	public Disk disk;
	public static int[][] BitMap = new int[8][32];
	String FileTable;
	String ChainedString;
	
	public Logic(){
		
		disk = new Disk();
		createFileTable();
		createBitWise();
		
	}
	
	public String getBlock(int blockNum){
		
		return disk.read(blockNum);
		
	}
	
	public void setBlock(int blockNum, String str){
		
		disk.write(blockNum, str);
		
	}
	
	public void createFileTable(){
		
		FileTable = "";
		setBlock(0, FileTable);
		
	}
	
	public void createBitWise(){
		
		updateBitMap(0,0,1);
		updateBitMap(0,1,1);
		
	}
	
	public void updateBitMap(int xLimit, int yLimit, int updateValue){
		
		for(int x = 0; x < 8; x++){
			
			for(int y = 0; y < 32; y++){
				
				if((x == xLimit && y == yLimit)){
					
					BitMap[x][y] = updateValue;
					
				}
				
			}
			
		}
		
		writeBitMap(BitMap);
		
	}
	
	public void updateFileTable(String filename, int startingBlock, int length, int allocationMethod){
		
		if(allocationMethod == 1 || allocationMethod == 2){
			
			FileTable = FileTable + filename + "\t" + startingBlock + "\t" + length + "\n";
			
		}else if(allocationMethod == 3){
			
			FileTable = FileTable + filename + "\t" + startingBlock + "\n";
			
		}
		
		setBlock(0, FileTable);
		
	}
	
	public void deleteFileTable(String filename){
		
		String fTable = "";
		
		String[] table = FileTable.split("\n");
		
		for(int i = 0; i < table.length; i++){
			
			if(!(table[i].contains(filename))){
				
				fTable = fTable + table[i] + "\n";
				
			}
			
		}
		
		setBlock(0,fTable);
		
	}
	
	public void writeBitMap(int[][] BitMap){
		
		String str = "";
		for(int x = 0; x < 8; x++){
			
			for(int y = 0; y < 32; y++){
				
				int num = BitMap[x][y];
				str = str + num;
				
			}
			
			str = str + "\n";
			
		}
		
		setBlock(1, str);
		
	}
	
	public int numberOfBlocks(byte[] fileContent){
		
		return (fileContent.length/512 + 1);
		
	}
	
	public static int[] flatten(int[][] arr) {
	    ArrayList<Integer> list = new ArrayList<Integer>();
	    for (int i = 0; i < arr.length; i++) {
	        // tiny change 1: proper dimensions
	        for (int j = 0; j < arr[i].length; j++) { 
	            // tiny change 2: actually store the values
	            list.add(arr[i][j]); 
	        }
	    }

	    // now you need to find a mode in the list.

	    // tiny change 3, if you definitely need an array
	    int[] vector = new int[list.size()];
	    for (int i = 0; i < vector.length; i++) {
	        vector[i] = list.get(i);
	    }
	    
	    return vector;
	}
	
	public void writeToBlocks(byte[] fileContent, int allocationMethod, String filename){
		
		if(allocationMethod == 1){
			
			contiguousAllocation(fileContent, filename);
			
		}else if(allocationMethod == 2){
			
			chainedAllocation(fileContent, filename);
			
		}else if(allocationMethod == 3){
			
			indexedAllocation(fileContent, filename);
			
		}
		
	}
	
	public void contiguousAllocation(byte[] fileContent, String filename){
		
		int blockNum = numberOfBlocks(fileContent);
		int[] bitArray = flatten(BitMap);
		boolean match = true;
		int startingBlock=0, endingBlock=0;
		int counter = 0;
		
		for(int x = 0; x < (bitArray.length - (blockNum-1)); x++){
			for(int y = x; y < (x + blockNum); y++){
				if(bitArray[y] == 1){
					match = false;
				}
				if(match == false)
					break;
			}
			if(match == true){
				startingBlock = x;
				endingBlock = x+(blockNum-1);
				break;
			}
			match = true;
		}
		
		for(int a = startingBlock; a <= endingBlock; a++){
			
			int row = a/32;
			int column =  a%32;
			updateBitMap(row,column,1);
			disk.disk[a] = Arrays.copyOfRange(fileContent, (counter * 512), ((counter+1) * 512));
			counter++;
			
		}
		
		updateFileTable(filename, startingBlock, blockNum, 1);
		
	}
	
	public void chainedAllocation(byte[] fileContent, String filename){
		
		int startingBlock = 0;
		int blockNum = numberOfBlocks(fileContent);
		int[] bitArray = flatten(BitMap);
		Random rand = new Random();
		String str = filename + "\t";
		
		for(int i = 0; i < blockNum; i++){
			
			int r;
			
			do{
				
				r = rand.nextInt(256);
				
			}while(bitArray[r] == 1);
			
			str = str + r + "\t";
			
			int row = r/32;
			int column =  r%32;
			updateBitMap(row,column,1);
			disk.disk[r] = Arrays.copyOfRange(fileContent, (i  * 512), ((i + 1) * 512));
			
			if(i == 0){
				
				startingBlock = r;
				
			}
			
		}
		
		updateFileTable(filename, startingBlock, blockNum, 2);
		
		str = str + "\n";
		ChainedString = ChainedString + str;
		
	}
	
	public void indexedAllocation(byte[] fileContent, String filename){
		
		int rootBlock = 0;
		int blockNum = numberOfBlocks(fileContent);
		int[] bitArray = flatten(BitMap);
		Random rand = new Random();
		String str = "";
		
		do{
			
			rootBlock = rand.nextInt(256);
			
		}while(bitArray[rootBlock] == 1);
		
		int row = rootBlock/32;
		int column =  rootBlock%32;
		updateBitMap(row,column,1);
		updateFileTable(filename, rootBlock, blockNum, 3);
		
		for(int i = 0; i < blockNum; i++){
			
			int r;
			
			do{
				
				r = rand.nextInt(256);
				
			}while(bitArray[r] == 1);
			
			str = str + r + "\n";
			
			row = r/32;
			column =  r%32;
			updateBitMap(row,column,1);
			disk.disk[r] = Arrays.copyOfRange(fileContent, (i  * 512), ((i + 1) * 512));
			
		}
		
		disk.disk[rootBlock] = str.getBytes();
		
	}
	
	public String displayFile(String filename, int allocationMethod){
		
		String display = null;
		
		if(allocationMethod == 1){
			
			display = contiguousDisplay(filename);
			
		}else if(allocationMethod == 2){
			
			display = chainedDisplay(filename);
			
		}else if(allocationMethod == 3){
			
			display = indexedDisplay(filename);
			
		}
		
		return display;
		
	}
	
	public String contiguousDisplay(String filename){
		
		String fName;
		int startingBlock = 0;
		int length = 0;
		String str = "";
		
		String[] table = FileTable.split("\n");
		
		for(int i = 0; i < table.length; i++){
			
			if(table[i].contains(filename)){
				
				String[] contents = table[i].split("\t");
				fName = contents[0];
				startingBlock = Integer.parseInt(contents[1]);
				length = Integer.parseInt(contents[2]);
				
			}
			
		}
		
		for(int i = 0; i < length; i ++){
			
			str = str + getBlock(startingBlock);
			startingBlock++;
			
		}
		
		return str;
		
	}
	
	public String chainedDisplay(String filename){
		
		String fName;
		int startingBlock = 0;
		String str = "";
		
		String[] table = ChainedString.split("\n");
		
		for(int i = 0; i < table.length; i++){
			
			if(table[i].contains(filename)){
				
				String[] contents = table[i].split("\t");
				fName = contents[0];
				
				for(int x = 1; x < contents.length; x++){
					
					int data = Integer.parseInt(contents[x]);
					str = str + getBlock(data);
					
				}
				
			}
			
		}
		
		return str;
		
	}
	
	public String indexedDisplay(String filename){
		
		String fName;
		int rootBlock = 0;
		String str;
		
		String[] table = FileTable.split("\n");
		
		for(int i = 0; i < table.length; i++){
			
			if(table[i].contains(filename)){
				
				String[] contents = table[i].split("\t");
				fName = contents[0];
				rootBlock = Integer.parseInt(contents[1]);
				
			}
			
		}
		
		str = getBlock(rootBlock);
		String[] index = str.split("\n");
		str = "";
		
		for(int i = 0; i < index.length; i++){
			
			str = str + getBlock(Integer.parseInt(index[i]));
			
		}
		
		return str;
		
	}
	
	public void deleteFile(String filename, int allocationMethod){
		
		if(allocationMethod == 1){
			
			contiguousDelete(filename);
			
		}else if(allocationMethod == 2){
			
			chainedDelete(filename);
			
		}else if(allocationMethod == 3){
			
			indexedDelete(filename);
			
		}
		
	}
	
	public void contiguousDelete(String filename){
		
		String fName = null;
		int startingBlock = 0;
		int length = 0;
		String str = "";
		String fTable = "";
		
		String[] table = FileTable.split("\n");
		
		for(int i = 0; i < table.length; i++){
			
			if(table[i].contains(filename)){
				
				String[] contents = table[i].split("\t");
				fName = contents[0];
				startingBlock = Integer.parseInt(contents[1]);
				length = Integer.parseInt(contents[2]);
				
			}
			
		}
		
		for(int a = startingBlock; a < (startingBlock + length); a++){
			
			int row = a/32;
			int column =  a%32;
			updateBitMap(row,column,0);
			disk.disk[a] = null;
			
		}
		
		deleteFileTable(fName);
		
	}
	
	public void chainedDelete(String filename){
		
		String fName = null;
		String str = "";
		
		deleteFileTable(filename);
		
		String[] table = ChainedString.split("\n");
		
		for(int i = 0; i < table.length; i++){
			
			if(table[i].contains(filename)){
				
				String[] contents = table[i].split("\t");
				fName = contents[0];
				
				for(int x = 1; x < contents.length; x++){
					
					int data = Integer.parseInt(contents[x]);
					
					int row = data/32;
					int column =  data%32;
					updateBitMap(row,column,0);
					disk.disk[data] = null;
					
				}
				
			}else{
				
				str = str + table[i] + "\n";
				
			}
			
		}
		
	}
	
	public void indexedDelete(String filename){
		
		String fName = null;
		int rootBlock = 0;
		String str;
		
		String[] table = FileTable.split("\n");
		
		for(int i = 0; i < table.length; i++){
			
			if(table[i].contains(filename)){
				
				String[] contents = table[i].split("\t");
				fName = contents[0];
				rootBlock = Integer.parseInt(contents[1]);
				
			}
			
		}
		
		str = getBlock(rootBlock);
		String[] index = str.split("\n");
		str = "";
		
		int row = rootBlock/32;
		int column =  rootBlock%32;
		updateBitMap(row,column,0);
		disk.disk[rootBlock] = null;
		
		for(int i = 0; i < index.length; i++){
			
			int pointer = Integer.parseInt(index[i]);
			row = pointer/32;
			column =  pointer%32;
			updateBitMap(row,column,0);
			disk.disk[pointer] = null;
			
		}
		
		deleteFileTable(fName);
		
	}
	
}

class Disk{
	
	public static byte[][] disk;
	final int BLOCKS = 256;
	final int BYTES = 512;
	
	public Disk(){
		
		disk = new byte[BLOCKS][BYTES];
		
	}
	
	public String read(int blockNum){
		
		String str =  new String(disk[blockNum]);
		return str;
		
	}
	
	public void write(int blockNum, String str){
		
		disk[blockNum] = str.getBytes();
		
	}
	
}