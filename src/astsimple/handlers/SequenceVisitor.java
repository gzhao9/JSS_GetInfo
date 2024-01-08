package astsimple.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import astsimple.handlers.SequenceInfo.MockSequence;

public class SequenceVisitor extends ASTVisitor {
  private ArrayList<SequenceInfo> sequences = new ArrayList<>();

  @Override
  public boolean visit(MethodDeclaration node) {
    Map<String, SequenceInfo> variables = new HashMap<>();
    if (node.getBody() != null) {
      // get the base info of the test case
      String methodName = node.getName().getIdentifier();
      ArrayList<String> annotationsName = SequenceProcessor.getAnnotations(node.modifiers());
      List<Statement> statements = node.getBody().statements();

      // go through each statement
      for (Statement statement : statements) {

        if (SequenceProcessor.isDefineMockedVariables(statement)) {
          // Put each mock object into the List variables
          SequenceProcessor.getVariables(statement, methodName, annotationsName, variables);
        }


        if (!((variables.keySet().size() > 0) && (statement instanceof ExpressionStatement))) {
          continue;
        }

        Expression method = ((ExpressionStatement) statement).getExpression();
        List<String> findedVariables = new ArrayList<>();

        if (
          !SequenceProcessor.isContainMockArguments(method, variables.keySet(), findedVariables)
        ) {
          continue;
        }

        for (String variable : findedVariables) {
          String strStatement =
            statement.toString().replace("\n", "").replace("\\", "\\\\").replace("\"", "\\\"");
          boolean isMockRelatedStatement =
            SequenceProcessor.isMockRelatedStatements(method, variable);
          // String fixStatment = fixedArgument((MethodInvocation) method);

          String fixStatment = "";
          String readToDecting = "";

          if (isMockRelatedStatement) {
            fixStatment = SequenceProcessor.fixToFormal((MethodInvocation) method);
            readToDecting = SequenceProcessor.processToDetection((MethodInvocation) method);
          }

          MockSequence sequence =
            new MockSequence(strStatement, isMockRelatedStatement, fixStatment, readToDecting);

          variables.get(variable).sequences.add(sequence);
        }


      }
    }
    sequences.addAll(variables.values());
    return true;
  }

  public ArrayList<SequenceInfo> getSequences() {
    return sequences;
  }
}
