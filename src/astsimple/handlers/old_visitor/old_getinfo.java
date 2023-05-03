package astsimple.handlers.old_visitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
//import org.json.*;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class old_getinfo extends AbstractHandler{
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	
	private boolean file_stated = false;
	private boolean method_found = false;
	private boolean import_calculated = false;
	
	private ArrayList<String> method_names = new ArrayList<>();
	private ArrayList<String> method_origin = new ArrayList<>();
	private ArrayList<Integer> method_count = new ArrayList<>();
	
	private ArrayList<ImportDeclaration> All_imports_in_one_file = new ArrayList<>();
	private ArrayList<FieldDeclaration> Mocked_with_annotation_file_level = new ArrayList<>();
	
	
	private ArrayList<String> ALL = new ArrayList<>();
//	private ArrayList<String> mock_methodnames = new ArrayList<>(Arrays.asList("mock","createMock","createNiceMock"));
	private ArrayList<String> mock_methodnames = new ArrayList<>();
	private ArrayList<String> mock_methodnames_springframework = new ArrayList<>();
	private ArrayList<String> mock_methodnames_springframework_special = new ArrayList<>();
	
	
	private ArrayList<String> mocked_classes = new ArrayList<>();//separarated by single file
	private ArrayList<String> mocked_classes_2 = new ArrayList<>();//not separated by single file, all mocked classes in one projet.
	
	private ArrayList<String> all_imports = new ArrayList<>();
	
	// "File path to the MockResarch repository, used to gather and use relevant files/scripts."
	private String MockResarchRepoPath = "C:\\Users\\10590\\OneDrive - stevens.edu\\phd program\\JSS Structure";
//	private String MockResarchRepoPath = "UndProjects";
	// "File path to directory where output will be located."
	private String OutputDirectoryPath = "C:\\Users\\10590\\OneDrive - stevens.edu\\phd program\\JSS_GetInfo\\src\\astsimple\\handlers\\DATA";
//	private String OutputDirectoryPath = "DATA";
	
	// "Label of output files. (XXX.txt, etc.) Set to name of Java Project analyzed, or leave null to attempt to auto-read project name."
	private String target = null;
	

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        // Get all projects in the workspace
        IProject[] projects = root.getProjects();
        
        //grab all the "create mock" methods from file.
        
        
        try {
//        	!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        	File file = new File(MockResarchRepoPath + "\\CreateMockMethods.txt");
			Scanner sc = new Scanner(file);
			
			while(sc.hasNextLine()) {
				mock_methodnames.add(sc.nextLine());
			}
			
			File file2 = new File(MockResarchRepoPath + "\\SpringframeworkMockMethods_fullname.txt");
			Scanner sc2 = new Scanner(file2);
			
			while(sc2.hasNextLine()) {
				mock_methodnames_springframework.add(sc2.nextLine());
			}

			File file3 = new File(MockResarchRepoPath + "\\SpringframeworkMockMethods_special.txt");
			Scanner sc3 = new Scanner(file3);
			
			while(sc3.hasNextLine()) {
				mock_methodnames_springframework_special.add(sc3.nextLine());
			}
			
			
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
//        System.out.println(mock_methodnames);
        
        
        // Loop over all projects
        for (IProject project : projects) {
            try {
                if (project.isNatureEnabled(JDT_NATURE)) {
                    analyseMethods(project);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        
        try {
			outputTXT(projects[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        return null;
    }

    private void analyseMethods(IProject project) throws JavaModelException {
        IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
        // parse(JavaCore.create(project));
        for (IPackageFragment mypackage : packages) {
            if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
            	
                createAST(mypackage);
            }

        }
    }

    private void createAST(IPackageFragment mypackage) throws JavaModelException {
        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {//this is file level
        	
//        	System.out.println(unit.getClass());
        	
        	
            // now create the AST for the ICompilationUnits
            CompilationUnit parse = parse(unit);
            MethodVisitor visitor = new MethodVisitor();
            parse.accept(visitor);
            
//            ImportVisitor imports = new ImportVisitor();
//            parse.accept(imports);
            
//            System.out.println("These are all the imports");
//            System.out.println(imports.getImports());
            
            method_names.clear();
        	method_origin.clear();
        	method_count.clear();
        	All_imports_in_one_file.clear();
        	Mocked_with_annotation_file_level.clear();
        	
        	
           
//            System.out.println(unit.getPath().toString());
//            System.out.println("Methods declared: ");
        	
        	
//            for (MethodDeclaration method : visitor.getMethods()) {
//            	
//                System.out.println("	Method name: " + method.getName()
//                        + " 	Return type: " + method.getReturnType2());
//            }
//            
//            System.out.println("Methods Invoked: ");
        	
//        	for(ImportDeclaration imports2 : imports.getImportDeclarations()) {
//        		all_imports.add(imports2.getName().getFullyQualifiedName());
////        		System.out.println(unit.getPath().toString());
////        		System.out.println(imports2.getName().getFullyQualifiedName()+ "3333333333333333333333333333333333333333333");
//        		
//        	}
        	
        	
        	
        	
        	
        	
//        	this is only for the case when we need to the the mocked classes from springframework methods, thus  
        	for(ClassInstanceCreation creation : visitor.getClassCreation()) {//method level

				if(creation.resolveTypeBinding() == null) {
    				continue;
    			}
        		
        		// if(mock_methodnames_springframework.contains(creation.getType().toString())) {
				if(mock_methodnames_springframework.contains((creation.resolveTypeBinding().getQualifiedName()))){
        			
        			if (creation.resolveTypeBinding() == null) {
        				continue;
        			}


        			if(file_stated == false) {
        				
//        				System.out.println(unit.getPath().toString());
//            			System.out.println("Methods Invoked: ");
        				
        				ALL.add(unit.getPath().toString());
            			ALL.add("Methods Invoked: ");
            			
            			all_imports.add(unit.getPath().toString());
            			
            			mocked_classes.add(unit.getPath().toString());
              	      
            			file_stated = true;
            		}
        			

					if(import_calculated == false) {
            			//make sure this only executes once inside the for loop
                		ImportVisitor imports = new ImportVisitor();
                        parse.accept(imports);
                        
                        for(ImportDeclaration x: imports.getImportDeclarations()) {
                        	All_imports_in_one_file.add(x);
                        }
                        
                        for(String y : imports.getAnnotationMocks()) {//From importVisitor.java, it only contains annotations that are use to create mock.
                        	
                        	mocked_classes.add("	" + y);
                        	mocked_classes_2.add(y);
                        }
                        
                        for(ImportDeclaration imports2 : All_imports_in_one_file) {
//                    		for(ImportDeclaration imports2 : imports.getImportDeclarations()) {
                        		all_imports.add("	" + imports2.getName().getFullyQualifiedName());
//                        		System.out.println(unit.getPath().toString());
//                        		System.out.println(imports2.getName().getFullyQualifiedName()+ "3333333333333333333333333333333333333333333");
                        		
                        }
                        
                        
                        import_calculated = true;
            		}
        			
        			
        			

        			
        			if(creation.resolveTypeBinding().getInterfaces().length > 0) {
//        				System.out.println("aaaaaaaaaaaaaaaaaaaa" + creation.resolveTypeBinding().getInterfaces().length);
//            			System.out.println("Implement - " + creation.resolveTypeBinding().getInterfaces()[0].getQualifiedName());
            			
            			mocked_classes.add("	"+ creation.resolveTypeBinding().getInterfaces()[0].getQualifiedName());
            			mocked_classes_2.add( creation.resolveTypeBinding().getInterfaces()[0].getQualifiedName());
        			}
        			else {
//        				System.out.println("Extend - " + creation.resolveTypeBinding().getSuperclass().getQualifiedName());
        				
        				mocked_classes.add("	"+creation.resolveTypeBinding().getSuperclass().getQualifiedName());
        				mocked_classes_2.add(creation.resolveTypeBinding().getSuperclass().getQualifiedName());
        			}
        			
        			
        		}
        		
        		
        	}
        	
        	
        	
            for(MethodInvocation method: visitor.getMethodInvocations()) {//method level
            	
            	if (method.resolveMethodBinding().getDeclaringClass().getQualifiedName()
						.startsWith("org.mockito.")) {
					System.out.print(method.resolveMethodBinding().getDeclaringClass().getQualifiedName());							
					System.out.print("|+|"+method.resolveMethodBinding().getDeclaringClass().getName());
					System.out.println("|+|"+method.getName());
					System.out.println();
				}
            	
            	
//            	System.out.println("	Method Name: "+method.getName());if(
            	if(method.resolveMethodBinding() != null 
            			&& method.resolveMethodBinding().getDeclaringClass()!= null
            			&& 
            				(method.resolveMethodBinding().getDeclaringClass().getQualifiedName()
            						.toLowerCase().contains("mock")
            				&&!method.resolveMethodBinding().getDeclaringClass().getQualifiedName()
                    			.toLowerCase().contains("apache"))) {
            		
            		
            		
//            		//make sure this only executes once inside the for loop
//            		ImportVisitor imports = new ImportVisitor();
//                    parse.accept(imports);
                    
                    
            		      		
                    
                    
            		
            		if(file_stated == false) {
//            			System.out.println(unit.getPath().toString());
//            			System.out.println("Methods Invoked: ");
            			
            			ALL.add(unit.getPath().toString());
            			ALL.add("Methods Invoked: ");
            			
            			all_imports.add(unit.getPath().toString());
//            			all_imports.add("import classes:");
            			
            			mocked_classes.add(unit.getPath().toString());
//            			mocked_classes.add("mocked classes");
            			
//            			try {
//                  	      FileWriter myWriter = new FileWriter("D:\\Stevens\\2021 summer general\\Mocking framework API calls data\\camel.txt");
//                  	      
//                  	      
//                  	      myWriter.write(unit.getPath().toString());
//                  	      myWriter.write("Methods Invoked: ");
//                  	      
//                  	      myWriter.close();
//                  	      
//                  	    } catch (IOException e) {
//                  	      
//                  	      e.printStackTrace();
//                  	    }
              	      
            			file_stated = true;
            		}
            		
            		if(import_calculated == false) {
            			//make sure this only executes once inside the for loop
                		ImportVisitor imports = new ImportVisitor();
                        parse.accept(imports);
                        
                        for(ImportDeclaration x: imports.getImportDeclarations()) {
                        	All_imports_in_one_file.add(x);
                        }
                        
                        for(String y : imports.getAnnotationMocks()) {//From importVisitor.java, it only contains annotations that are use to create mock.
                        	
                        	mocked_classes.add("	" + y);
                        	mocked_classes_2.add(y);
                        }
                        
                        for(ImportDeclaration imports2 : All_imports_in_one_file) {
//                    		for(ImportDeclaration imports2 : imports.getImportDeclarations()) {
                        		all_imports.add("	" + imports2.getName().getFullyQualifiedName());
//                        		System.out.println(unit.getPath().toString());
//                        		System.out.println(imports2.getName().getFullyQualifiedName()+ "3333333333333333333333333333333333333333333");
                        		
                        }
                        
                        
                        import_calculated = true;
            		}
            		
//            		System.out.println("777777777777777777777777"+All_imports_in_one_file);      
            		
            		
            		
            		
            		
            		
            		for(int i = 0;i < method_names.size(); i++) {
            			if(method_names.get(i).equals(method.getName().toString()) && 
            					method_origin.get(i).equals(method.resolveMethodBinding().getDeclaringClass().getQualifiedName().toString())) {
            				method_count.set(i, method_count.get(i) + 1);
            				method_found = true;
            				break;
            			}
            		}
            		
            		if(!method_found) {
            			method_names.add(method.getName().toString());
                		method_origin.add(method.resolveMethodBinding().getDeclaringClass().getQualifiedName());
                		method_count.add(1);
            		}
            		
            		
            		
//            		System.out.println("	Method Name: " + method.getName());
//            		
//                	ITypeBinding binding = method.resolveMethodBinding().getDeclaringClass();
//                	
////                	ITypeBinding binding2 = method.resolveMethodBinding().getDeclaringClass();
//                	
//                	if(binding!=null) {
//                		System.out.println("	Class origin: " + binding.getQualifiedName());
//                	}
            		
            		
            		
            		
            		
//            		this part is what I use to test to get the dependency classes, which are the classes that are mocked, you can ignore this
//            		for now.
            		
//            		System.out.println("333333333" + method.getName().toString());
//                	System.out.println("333333333" + method.resolveMethodBinding().getDeclaringClass().getQualifiedName());
//                	System.out.println("333333333" + method.resolveTypeBinding().getQualifiedName());
//                	System.out.println("333333333" + method.getParent().toString());
                	
                	
                	if(mock_methodnames.contains(method.getName().toString())) {
//                		System.out.println(method.getName().getFullyQualifiedName());
                		mocked_classes.add("	" + method.resolveTypeBinding().getQualifiedName());
                		mocked_classes_2.add(method.resolveTypeBinding().getQualifiedName());
                		
                	}

					if(method.getName().toString().equals("build")   &&  
                			mock_methodnames_springframework_special.contains(method.resolveMethodBinding().getDeclaringClass().getQualifiedName())) {
//                		System.out.println(method.resolveMethodBinding().getDeclaringClass().getQualifiedName());
                		mocked_classes.add("	" + method.getParent().toString());
                		mocked_classes_2.add(method.getParent().toString());
                	}

            	}
            	
            	
            	
            	
            }
            
            
            
            
            
            for(int i = 0; i< method_names.size();i++) {
//            	System.out.println("	Method name: " + method_names.get(i));
//            	System.out.println("	Class origin: " + method_origin.get(i));
//            	System.out.println("	Count: " + method_count.get(i));
//            	System.out.println(" ");
            	
            	
            	ALL.add("	Method name: " + method_names.get(i));
            	ALL.add("	Class origin: " + method_origin.get(i));
            	ALL.add("	Count: " + method_count.get(i));
            	ALL.add(" ");
            	
//            	try {
//            	      FileWriter myWriter = new FileWriter("D:\\Stevens\\2021 summer general\\Mocking framework API calls data\\camel.txt");
//            	      
//            	      myWriter.write("		Method name: " + method_names.get(i));
//            	      myWriter.write("		Class origin: " + method_origin.get(i));
//            	      myWriter.write("		Count: " + method_count.get(i));
//            	      myWriter.write(" ");
//            	      
//            	      myWriter.close();
//            	      System.out.println("Successfully wrote to the file.");
//            	    } catch (IOException e) {
//            	      System.out.println("An error occurred.");
//            	      e.printStackTrace();
//            	    }
            }
            
            file_stated = false;
            method_found = false;
            import_calculated = false;
            
          
        }
    }

    /**
     * Reads a ICompilationUnit and creates the AST DOM for manipulating the
     * Java source file
     *
     * @param unit
     * @return
     */

    private static CompilationUnit parse(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS16);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null); // parse
    }
    
    
    private void outputTXT(IProject project) throws IOException{
    	
    	if (target == null) {
    		target = project.getName();
    	}
//    	String target = project.getName();
    	
//    	String target = "any23";
    	
    	
//    	PrintWriter writer = new PrintWriter("D:\\Stevens\\2021 summer general\\Mocking framework API calls data"
//    				+ "\\test.txt", "UTF-8");
//    		
//    	writer.println("line 3");
//    	writer.println("line 4");
//    	
//    	writer.close();
    	
    	PrintWriter writer = new PrintWriter(OutputDirectoryPath + "\\Mocking framework API calls data\\"
				+ target + ".txt", "UTF-8");
    	
    	
    	for(String x: ALL) {
    		writer.println(x);
    	}
    	
    	writer.close();
    	
    	
    	PrintWriter writer2 = new PrintWriter(OutputDirectoryPath + "\\RQ3\\RQ3 data\\" + target + 
    			"Mocked classes.txt","UTF-8");
    	
    	for(String item: mocked_classes_2) {
    		writer2.println(item);
    	}
    	
    	writer2.close();
    	
    	
    	PrintWriter writer3 = new PrintWriter(OutputDirectoryPath + "\\RQ2\\single file import classes\\" + target + 
    			" Import classes .txt","UTF-8");
    	
    	for(String item: all_imports) {
    		writer3.println(item);
    	}
    	
    	writer3.close();
    	
    	
    	PrintWriter writer4 = new PrintWriter(OutputDirectoryPath + "\\RQ2\\single file mocked classes\\" + target + 
    			"Mocked classes by file.txt","UTF-8");
    	
    	for(String item: mocked_classes) {
    		writer4.println(item);
    	}
    	
//    	for(FieldDeclaration item2: Mocked_with_annotation) {
//    		writer4.println(item2);
//    	}
    	
    	writer4.close();
    	
    	
//    	System.out.println(ALL.size());
    	
    	ALL.clear();
    	
//    	System.out.println(mocked_classes);
    	mocked_classes.clear();
    	
    	
//    	System.out.println("these are all the imports");
//    	System.out.println(all_imports.size());
    	all_imports.clear();
    	
//    	System.out.println(mock_methodnames_springframework);
    	
    	mocked_classes_2.clear();
    }
    
}
