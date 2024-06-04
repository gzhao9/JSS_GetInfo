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

public class GetInfo extends AbstractHandler {
  ArrayList<SequenceInfo> sequences = new ArrayList<>();
  private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    sequences.clear();
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
    String outPutPath =
//      "C:\\Users\\10590\\OneDrive - stevens.edu\\PHD\\2023 Fall\\clone detection\\parserResult\\"
    	"C:\\Users\\10590\\OneDrive - stevens.edu\\PHD\\2024 S\\mock clone detection\\parserResult\\"
        + projects[0].getName() + "-Result.json";
    printResults(outPutPath);

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

  private void analyseMethods(IProject project) throws CoreException {
    IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
    // parse(JavaCore.create(project));
    for (IPackageFragment mypackage : packages) {
      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
        createAST(mypackage);
      }

    }
  }

  private void createAST(IPackageFragment mypackage) throws CoreException {
    for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
      // now create the AST for the ICompilationUnits
      if (!importMock(unit)) {
        continue;
      }
      CompilationUnit parse = parse(unit);
      SequenceVisitor visitor = new SequenceVisitor();
      parse.accept(visitor);

      String packageName=unit.getPackageDeclarations()[0].getElementName().toString();
      for (SequenceInfo sequence : visitor.getSequences()) {
        sequence.packages=packageName;
        sequence.filePath = unit.getPath().toString();
        sequences.add(sequence);
      }
    }
  }



  private void printResults(String path) {
    if (sequences.size() > 0) {
      try (FileOutputStream fos = new FileOutputStream(path)) {
        fos.write("[\n".getBytes());
        for (int i = 0; i < sequences.size(); i++) {
          fos.write(sequences.get(i).toJson().getBytes());

          fos.write((i < sequences.size() - 1 ? ",\n" : "\n]").getBytes());
        }
        fos.flush();
        System.out.println("Text has  been  written to " + (new File(path)).getAbsolutePath() + '\t'
          + sequences.size());
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
