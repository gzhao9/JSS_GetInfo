package astsimple.handlers;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class GetInfo extends AbstractHandler {
	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";
	private ArrayList<String> mockito_arr = new ArrayList<>();
	private ArrayList<String> easymock_arr = new ArrayList<>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		IProject[] projects = root.getProjects();

		// Loop over all projects
		for (IProject project : projects) {
			try {
				if (project.isNatureEnabled(JDT_NATURE)) {
					analyseMethods(project);
				}
			} catch (CoreException e) {
				System.out.println("sjip "+project.getName());
			}
		}

		write_to_txt(projects[0].getName());

		return null;
	}

	private void analyseMethods(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		// parse(JavaCore.create(project));
		for (IPackageFragment mypackage : packages) {
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

				for (ICompilationUnit unit : mypackage.getCompilationUnits()) {// this is file level

					// now create the AST for the ICompilationUnits
					CompilationUnit parse = parse(unit);
					MethodVisitor visitor = new MethodVisitor();
					parse.accept(visitor);
					for (MethodInvocation method : visitor.getMethodInvocations()) {// method level
						
						if (method.resolveMethodBinding().getDeclaringClass().getQualifiedName()
								.startsWith("org.mockito.")) {
							mockito_arr.add(unit.getPath().toString() + ","
//									+ method.resolveMethodBinding().getDeclaringClass().getQualifiedName() + "|||"
//									+ method.resolveMethodBinding().getDeclaringClass().getClass().getName() + "|||"
									+ method.getName());
						}
						if (method.resolveMethodBinding().getDeclaringClass().getQualifiedName()
								.startsWith("org.easymock")) {
							easymock_arr.add(unit.getPath().toString() + ","
//									+ method.resolveMethodBinding().getDeclaringClass().getQualifiedName() + "|||"
//									+ method.resolveMethodBinding().getDeclaringClass().getClass().getName() + "|||"
									+ method.getName());
						}


					}
				}

			}
		}

	}

	private void write_to_txt(String project_name) {
		try {
			if (mockito_arr.size() > 0) {
				PrintWriter mockito_out = new PrintWriter(
						"C:\\Users\\10590\\OneDrive - stevens.edu\\phd program\\JSS_GetInfo\\src\\astsimple\\handlers\\new_RQ2\\mockito\\"
								+ project_name + ".csv",
						"UTF-8");
				mockito_out.println("file_path,method");
				for (String x : mockito_arr) {

					mockito_out.println(x);
				}
				mockito_out.close();
			}
			else {
				System.out.println("no mockito_out");
			}
			
			if (easymock_arr.size() > 0) {
				PrintWriter easymock_out = new PrintWriter(
						"C:\\Users\\10590\\OneDrive - stevens.edu\\phd program\\JSS_GetInfo\\src\\astsimple\\handlers\\new_RQ2\\easymock\\"
								+ project_name + ".csv",
						"UTF-8");
				easymock_out.println("file_path,method");
				for (String x : easymock_arr) {

					easymock_out.println(x);
				}

				easymock_out.close();
			}else {
				System.out.println("no easymock_out");
			}

			System.out.println(project_name + " is done!");
		} catch (IOException e) {
			System.out.println(e);
		}

	}

	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS16);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
}
