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
				System.out.println("error in "+project.getName());
			}
		}

		print_to_csv(projects[0].getName());

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
					for (MethodInvocation project_MethodInvocation : visitor.getMethodInvocations()) {// method level

						if (project_MethodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName()
								.startsWith("org.mockito.")) {
							mockito_arr.add(unit.getPath().toString() + ","
//									+ method.resolveMethodBinding().getDeclaringClass().getQualifiedName() + "|||"
//									+ method.resolveMethodBinding().getDeclaringClass().getClass().getName() + "|||"
									+ project_MethodInvocation.getName());
						}
						if (project_MethodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName()
								.startsWith("org.easymock")) {
							easymock_arr.add(unit.getPath().toString() + ","
//									+ method.resolveMethodBinding().getDeclaringClass().getQualifiedName() + "|||"
//									+ method.resolveMethodBinding().getDeclaringClass().getClass().getName() + "|||"
									+ project_MethodInvocation.getName());
						}

					}
				}

			}
		}

	}

	private void print_to_csv(String project_name) {
		String mockito_out = "new_RQ2\\mockito\\" + project_name + ".csv";
		String easymock_out = "new_RQ2\\easymock\\" + project_name + ".csv";

		// check weather have mockito api
		if (mockito_arr.size() > 0) {
			try (FileOutputStream fos = new FileOutputStream(mockito_out)) {
				for (String x : mockito_arr) {
					fos.write(x.getBytes());
				}

				// Flush the written bytes to the file
				fos.flush();

				System.out.println("Text has  been  written to " + (new File(mockito_out)).getAbsolutePath());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}else {
			System.out.println("no mockito api");
		}
		// check weather have easymock api
		if (easymock_arr.size() > 0) {
			try (FileOutputStream fos = new FileOutputStream(easymock_out)) {
				for (String x : easymock_arr) {
					fos.write(x.getBytes());
				}
				fos.flush();

				System.out.println("Text has  been  written to " + (new File(easymock_out)).getAbsolutePath());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}else {
			System.out.println("no easymock api");
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
