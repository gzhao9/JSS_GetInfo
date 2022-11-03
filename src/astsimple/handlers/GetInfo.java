package astsimple.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

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
import org.eclipse.jdt.core.dom.MethodInvocation;

public class GetInfo extends AbstractHandler {
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
//		System.out.println(projects.length);
//		get the mockito and easy mock api
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
			if (import_mock.getElementName().contains("powermock") || import_mock.getElementName().contains("springframework")) {
				return true;
			}
		}
		return false;
	}

	private void GetMockitoEasyMock_API(IProject[] projects) throws CoreException {

		ArrayList<String> PowerMock_arr = new ArrayList<>();
		ArrayList<String> SpringFramework_arr = new ArrayList<>();
		ArrayList<String> have_mock = new ArrayList<>();

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
									Top2_4Visitor visitor = new Top2_4Visitor();
									parse.accept(visitor);
									System.out.println(unit.getPath().toString());
									for (MethodInvocation Mockito_method : visitor.getPowerMockMethodInvocations()) {
										PowerMock_arr
												.add(unit.getPath().toString() + "," + Mockito_method.getName() + '\n');
									}

									for (MethodInvocation EasyMock_method : visitor.getSpringFrameworkMethodInvocations()) {
										SpringFramework_arr.add(
												unit.getPath().toString() + "," + EasyMock_method.getName() + '\n');
									}
								} catch (NullPointerException e) {
									System.err.println(unit.getPath().toString());
									err_arr.add(unit.getPath().toString() + '\n');
								}
							} 
//							else {
//								try {
//									RQ4_visitor visitor = new RQ4_visitor();
//									parse.accept(visitor);
//									for (String Mock_method : visitor.getContainMock()) {
//										have_mock.add(unit.getPath().toString() + "," + Mock_method + '\n');
//									}
//								} catch (Exception e) {
//								}
//							}

						}

					}
				}
			}
		}
//		print_to_csv(projects[0].getName());

		String PowerMock_out = "new_RQ2\\powerMock\\" + projects[0].getName() + ".csv";
		String SpringFramework_out = "new_RQ2\\springframework\\" + projects[0].getName() + ".csv";
		String err = "new_RQ2\\err\\" + projects[0].getName() + ".csv";
		String RQ4_out = "RQ4\\" + projects[0].getName() + ".csv";
		System.out.println("Start writing");
		print_arr_to_csv(PowerMock_arr, PowerMock_out);

		print_arr_to_csv(SpringFramework_arr, SpringFramework_out);

		print_arr_to_csv(have_mock, RQ4_out);

		print_arr_to_csv(err_arr, err);

	}

	private void print_arr_to_csv(ArrayList<String> data, String path) {
		if (data.size() > 0) {
			try (FileOutputStream fos = new FileOutputStream(path)) {
				fos.write("file_path,method\n".getBytes());
				for (String x : data) {
					fos.write(x.getBytes());
				}

				// Flush the written bytes to the file
				fos.flush();

				System.out.println("Text has  been  written to " + (new File(path)).getAbsolutePath()+'\t'+data.size());

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
