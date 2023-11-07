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


public class SequenceGetter extends AbstractHandler {
    private ArrayList<String> mockedClasses = new ArrayList<>();
    private ArrayList<String> mockedMethods = new ArrayList<>();
    private ArrayList<String> errorArray = new ArrayList<>();
    private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject[] projects = root.getProjects();

        try {
            getMockitoEasyMockApi(projects);
        } catch (CoreException e) {
            // Handle the exception appropriately
        }
        printResults(projects[0].getName());
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

    private void getMockitoEasyMockApi(IProject[] projects) throws CoreException {
        for (IProject project : projects) {
            if (project.isNatureEnabled(JDT_NATURE)) {
                analyzeJavaProjectPackages(JavaCore.create(project).getPackageFragments());
            }
        }
    }

    private void analyzeJavaProjectPackages(IPackageFragment[] packages) throws CoreException {
        for (IPackageFragment myPackage : packages) {
            if (myPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                analyzePackageCompilationUnits(myPackage.getCompilationUnits());
            }
        }
    }

    private void analyzePackageCompilationUnits(ICompilationUnit[] compilationUnits) throws CoreException {
        for (ICompilationUnit unit : compilationUnits) {
            CompilationUnit parsedUnit = parse(unit);
            if (importMock(unit)) {
                analyzeUnit(unit, parsedUnit);
            }
        }
    }

    private void analyzeUnit(ICompilationUnit unit, CompilationUnit parse) throws JavaModelException {
        try {

        } catch (NullPointerException e) {
        }
    }

    private void printResults(String projectName) {
        String mockObjectPath = "C:\\Users\\gzhao9\\OneDrive - stevens.edu\\PHD\\2023 Fall\\Mocking clone\\" + projectName + " Class_level.csv";
        String mockMethodPath = "C:\\Users\\gzhao9\\OneDrive - stevens.edu\\PHD\\2023 Fall\\Mocking clone\\" + projectName + " Method_level.csv";
        printArrayToCsv(mockedClasses, mockObjectPath);
        printArrayToCsv(mockedMethods, mockMethodPath);
    }

    private void printArrayToCsv(ArrayList<String> data, String path) {
        if (!data.isEmpty()) {
            try (FileOutputStream fos = new FileOutputStream(path)) {
                fos.write("path|test case|annotations|object|label\n".getBytes());
                for (String record : data) {
                    fos.write(record.getBytes());
                }
                fos.flush();
                System.out.println("Text has been written to " + (new File(path)).getAbsolutePath() + '\t' + data.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
