package astsimple.sequencepaser;

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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import astsimple.handlers.SequenceInfo;
import astsimple.handlers.SequenceVisitor;


public class SequenceGetter extends AbstractHandler {
  ArrayList<SequenceInfo> sequences = new ArrayList<>();
    private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IWorkspaceRoot root = workspace.getRoot();
      // Get all projects in the workspace
      IProject[] projects = root.getProjects();
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
      // printResults(projects[0].getName());

        return null;
    }

    private boolean importMock(ICompilationUnit unit) throws CoreException {
        if (unit.getImports().length <= 0) {
            return false;
        }
        for (IImportDeclaration importDeclaration : unit.getImports()) {
            if (importDeclaration.getElementName().contains("mockito")) {
                return true;
            }
        }
        return false;
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
      for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
        // now create the AST for the ICompilationUnits
        CompilationUnit parse = parse(unit);
        SequenceVisitor visitor = new SequenceVisitor();
        parse.accept(visitor);
        analyzeUnit(visitor);
        for (SequenceInfo sequence : visitor.getSequences()) {
          System.out.print(sequence.toJson());
          }

      }
    }

    private void analyzeUnit(SequenceVisitor visitor) throws JavaModelException {
        try {
        sequences.addAll(visitor.getSequences());
        } catch (NullPointerException e) {
        }
    }

    private void printResults(String projectName) {
      String path = "";
      if (sequences.size() > 0) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
          fos.write("[\n".getBytes());
          for (SequenceInfo x : sequences) {
            fos.write("\n".getBytes());
            fos.write(x.toJson().getBytes());
          }
          fos.flush();
          System.out.println("Text has  been  written to " + (new File(path)).getAbsolutePath()
            + '\t' + sequences.size());
        } catch (Exception e2) {
          e2.printStackTrace();
        }
      }
    }


    private static CompilationUnit parse(ICompilationUnit unit) {
      ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null);
    }
}
