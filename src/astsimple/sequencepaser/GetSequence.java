package astsimple.sequencepaser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import astsimple.sequencepaser.TestCaseObjectVisitor.TestCaseObject;

public class GetSequence extends AbstractHandler {
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
//	private IWorkspace workspace = ResourcesPlugin.getWorkspace();
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();

		try {
			GetMockitoEasyMock_API(projects);
		} catch (CoreException e) {
		}

		return null;
	}

	private boolean Import_mock(ICompilationUnit unit) throws CoreException {
		if (unit.getImports().length <= 0) {
			return false;
		}
		for (IImportDeclaration import_mock : unit.getImports()) {
			if (import_mock.getElementName().contains("mockito")) {
				return true;
			}
		}
		return false;
	}

	private void GetMockitoEasyMock_API(IProject[] projects) throws CoreException {
		ArrayList<String> MockedClass = new ArrayList<>();
		ArrayList<String> MockedMethod = new ArrayList<>();
		ArrayList<String> err_arr = new ArrayList<>();

//		go throw all the project
		for (IProject project : projects) {

			if (project.isNatureEnabled(JDT_NATURE)) {

				IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
				for (IPackageFragment mypackage : packages) {
					if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

						for (ICompilationUnit unit : mypackage.getCompilationUnits()) {// this is file level

							// now create the AST for the ICompilationUnits
							CompilationUnit parse = parse(unit);
							if (Import_mock(unit)) {

								try {
									TestCaseObjectVisitor mockobjectvisitor = new TestCaseObjectVisitor();

									parse.accept(mockobjectvisitor);
//									System.out.println(unit.getPath().toString());							
									String packageName=unit.getPackageDeclarations()[0].getElementName().toString();
							        String fileName = unit.getElementName().toString().replace(".java", "");
							        String longName = packageName + '.' + fileName;
									System.out.println("   "+longName);
									Map<String, TestCaseObject> testCases = mockobjectvisitor.getTestCaseRecord();
//									 get the class level
									for (String testCase : testCases.keySet()) {
										Map<String, String> object_records = testCases.get(testCase).object_recording;
										Map<String, String> method_records = testCases.get(testCase).method_recording;
										
										for (String objectReocrd : object_records.keySet()) {
											MockedClass.add(unit.getPath().toString()+ "|" +longName + "." + testCase + "|"+String.join(",", testCases.get(testCase).annotations)+ "|"
													+ objectReocrd + "|" + object_records.get(objectReocrd) + "\n");
										}
										// get the method level
										for (String method_record : method_records.keySet()) {
											MockedMethod.add(unit.getPath().toString()+ "|" +longName + "." + testCase + "|"+String.join(",", testCases.get(testCase).annotations)+ "|"
													+ method_record + "|" + method_records.get(method_record) + "\n");
										}
									}
//									

								} catch (NullPointerException e) {
									System.err.println(unit.getPath().toString());
									err_arr.add(unit.getPath().toString() + '\n');
								}

							}

						}

					}

				}
			}
		}
		
		String MockObjectPath2 = "C:\\Users\\gzhao9\\OneDrive - stevens.edu\\PHD\\2023 Fall\\Mocking clone\\"
				+ projects[0].getName() + " Class_level.csv";
		print_arr_to_csv(MockedClass, MockObjectPath2);

	}

	private void print_arr_to_csv(ArrayList<String> data, String path) {
		if (data.size() > 0) {
			try (FileOutputStream fos = new FileOutputStream(path)) {
        fos.write("path|test case|annotations|object|label\n".getBytes());
				for (String x : data) {
					fos.write(x.getBytes());
				}

				// Flush the written bytes to the file
				fos.flush();

				System.out.println(
						"Text has  been  written to " + (new File(path)).getAbsolutePath() + '\t' + data.size());

			} catch (Exception e2) {
				e2.printStackTrace();

			}
		}

	}

	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS16);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
//		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
}
