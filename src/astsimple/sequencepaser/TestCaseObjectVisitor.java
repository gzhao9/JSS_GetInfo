package astsimple.sequencepaser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;


public class TestCaseObjectVisitor extends ASTVisitor {
  ArrayList<SequenceInfo> sequences = new ArrayList<>();
  @Override
  public boolean visit(MethodDeclaration node) {
    if (node.getBody() != null) {
      String methodName = node.getName().getIdentifier();
      ArrayList<String> annotationsName = getAnnotations(node.modifiers());
      List<Statement> statements = node.getBody().statements();
      Map<String, SequenceInfo> variables = getVariables(statements, methodName);
    }

    return true;
  }

  private ArrayList<String> getAnnotations(List<IExtendedModifier> modifiers) {
    ArrayList<String> modifiersNames = new ArrayList<>();
    for (IExtendedModifier modifier : modifiers) {
      if (modifier.isAnnotation()) {
        modifiersNames.add(modifier.toString());
      }
    }
    return modifiersNames;
  }

  private Map<String, SequenceInfo> getVariables(List<Statement> statements, String methodName) {
    Map<String, SequenceInfo> variables = new HashMap<>();
    for(Statement statement:statements) {
      if (statement instanceof TypeDeclarationStatement) {

      }

    }
    return variables;
  }

  private ArrayList<String> getExpressionStatement(List<Statement> statements) {
    ArrayList<String> expressionStatements = new ArrayList<>();
    for (Statement statement : statements) {
      if (statement instanceof ExpressionStatement) {
      
    }
  }
  return expressionStatements;
  }
}
