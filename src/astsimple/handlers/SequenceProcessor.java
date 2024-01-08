package astsimple.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class SequenceProcessor {
  // get the annotations of the test case
  public static ArrayList<String> getAnnotations(List<IExtendedModifier> modifiers) {
    ArrayList<String> modifiersNames = new ArrayList<>();
    for (IExtendedModifier modifier : modifiers) {
      if (modifier.isAnnotation()) {
        modifiersNames.add(modifier.toString().replace("\\", "\\\\").replace("\"", "\\\""));
      }
    }
    return modifiersNames;
  }

  // get each object which is mock value.
  public static void getVariables(Statement statement, String methodName,
    ArrayList<String> annotationsName, Map<String, SequenceInfo> variables) {
    // get the statement info
    VariableDeclarationStatement variable = (VariableDeclarationStatement) statement;
    SequenceInfo sequenceStatement = new SequenceInfo();

    VariableDeclarationFragment nameFragment =
      (VariableDeclarationFragment) variable.fragments().get(0);
    String variableName = nameFragment.getName().getIdentifier();

    IVariableBinding binding = nameFragment.resolveBinding();
    // check the compile success
    if (((VariableDeclarationStatement) statement).getType().resolveBinding() != null) {
      String valuetypeName =
        ((VariableDeclarationStatement) statement).getType().resolveBinding().getName();
      // set the info of sequenceStatement

      String typeName = binding != null ? (binding.getType().getQualifiedName()) : "";

      sequenceStatement.sequenceName = variableName;
      sequenceStatement.annotationsName = annotationsName;
      sequenceStatement.testcase = methodName;
      sequenceStatement.mockedDependency = typeName;
      sequenceStatement.valuetypeName = valuetypeName;
      sequenceStatement.creationStatement =
        statement.toString().replace("\n", "").replace("\\", "\\\\").replace("\"", "\\\"");
      // put to variables sequence
      variables.put(variableName, sequenceStatement);
    }
  }

  // check weather the value is a mock object
  public static boolean isDefineMockedVariables(Statement statement) {
    if (statement instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement variable = (VariableDeclarationStatement) statement;
      VariableDeclarationFragment initializerFragment =
        (VariableDeclarationFragment) variable.fragments().get(0);
      if (initializerFragment.getInitializer() instanceof MethodInvocation) {
        MethodInvocation createMethod = (MethodInvocation) initializerFragment.getInitializer();
        if (createMethod.getName().toString().toLowerCase().contains("mock")) {
          return true;
        }
      }
    }
    return false;
  }

  // check a statement include a mock object
  public static boolean isContainMockArguments(Expression method, Set<String> mockObjectSet,
    List<String> findedVariables) {
    boolean argumentIsMockValue = false;
    boolean expressionIsMockValue = false;
    if (method instanceof MethodInvocation) {
      // check the arguments parts include mock
      MethodInvocation invocation = (MethodInvocation) method;
      for (Object element : invocation.arguments()) {
        if (element instanceof MethodInvocation) {
          argumentIsMockValue = argumentIsMockValue
            || isContainMockArguments((Expression) element, mockObjectSet, findedVariables);
        }
        String variable = element.toString();
        if (mockObjectSet.contains(variable)) {
          argumentIsMockValue = true;
          findedVariables.add(variable);
        }
      }
      // check expression part include mock argument
      Expression subMethod = invocation.getExpression();
      if (subMethod != null) {
        if (subMethod instanceof MethodInvocation) {
          expressionIsMockValue = isContainMockArguments(subMethod, mockObjectSet, findedVariables);
        }
        String expressions = subMethod.toString();

        // if expression is a mock object, just return true
        if (mockObjectSet.contains(expressions)) {
          argumentIsMockValue = true;
          findedVariables.add(expressions);
        }
      }

    }
    boolean methodInKeySet = argumentIsMockValue || expressionIsMockValue;
    return methodInKeySet;
  }

  public static boolean isMockRelatedStatements(Expression method, String value) {
    // String statement = method.toString();
    boolean inArgument = false;
    boolean methodIsMock = false;
    boolean expressionIsMock = false;

    if (method instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) method;
      IMethodBinding binding = invocation.resolveMethodBinding();
      Expression expression = invocation.getExpression();

      if ((binding != null) && (binding.getDeclaringClass() != null)) {
        String bindingName = binding.getDeclaringClass().getQualifiedName();
        boolean isNotOngoingStubing = !bindingName.contains("OngoingStubbing");
        methodIsMock = bindingName.startsWith("org.mockito");
        methodIsMock = methodIsMock && isNotOngoingStubing;
        if (methodIsMock) {
          for (Object element : invocation.arguments()) {
            while (element instanceof MethodInvocation) {
              element = ((MethodInvocation) element).getExpression();
            }
            if (element != null) {
              String simpleName = element.toString();
              if (simpleName.equals(value)) {
                inArgument = true;
              }
            }
          }
        }
      }

      if (expression != null) {
        expressionIsMock = isMockRelatedStatements(expression, value);
        String expressionName = expression.toString();
        if (expressionName.equals(value)) {
          methodIsMock = true;
        }
      }
    }
    boolean methodInKeySet = (inArgument && methodIsMock) || expressionIsMock;
    return methodInKeySet;
  }

  public static String fixToFormal(MethodInvocation method) {
    String expressionMethod = "";
    String currentMethod = "";
    Expression expression = method.getExpression();
    if (expression instanceof MethodInvocation) {
      expressionMethod = fixToFormal((MethodInvocation) expression) + "().";
    }
    IMethodBinding binding = method.resolveMethodBinding();

    if ((binding != null) && (binding.getDeclaringClass() != null)) {
      String bindingName = binding.getDeclaringClass().getQualifiedName();
      if (bindingName.startsWith("org.mockito")) {
        currentMethod = method.getName().getIdentifier();
      } else {
        currentMethod = "foo";
      }
    }
    return (expressionMethod + currentMethod + "()").replace("()()", "()");
  }

  public static String processToDetection(MethodInvocation method) {
    String result = "";
    String expressionMethod = "";
    String currentMethod = "";

    Expression expression = method.getExpression();
    if (expression instanceof MethodInvocation) {
      expressionMethod = processToDetection((MethodInvocation) expression);
      if (expressionMethod.length() > 0) {
        expressionMethod = expressionMethod + ".";
      }
    }
    IMethodBinding binding = method.resolveMethodBinding();

    if ((binding != null) && (binding.getDeclaringClass() != null)) {
      String bindingName = binding.getDeclaringClass().getQualifiedName();

      String currentMethodName = method.getName().getIdentifier();
      if (bindingName.contains("BDDStubber")) {
        currentMethodName = BDDtoNormalMockMethod.setStubberToNormal(currentMethodName);
      } else if (bindingName.contains("BDDMyOngoingStubbing")) {
        currentMethodName = BDDtoNormalMockMethod.setBDDMyOngoingToNormal(currentMethodName);
      }
      if (bindingName.startsWith("org.mockito")) {
        if (currentMethodName.equals("when") || currentMethodName.equals("given")) {
          if (method.arguments().size() > 0) {
            if (method.arguments().get(0) instanceof MethodInvocation) {
              MethodInvocation argumentMethod = (MethodInvocation) method.arguments().get(0);

              String parameterTypes = getFormattedMethodSignature(argumentMethod);
              currentMethod = "when(" + parameterTypes + ")";
            } else {
              String argument = getArgumentReturnType(method);
              currentMethod = "when(" + argument + ")";
            }
          }
        } else if (
          currentMethodName.equals("thenReturn") || currentMethodName.equals("doReturn")
            || currentMethodName.equals("doThrow") || currentMethodName.equals("thenThrow")
        ) {
          String argument = getArgumentReturnType(method);
          currentMethod = currentMethodName + "(" + argument + ")";
        } else if (
          currentMethodName.equals("doNothing") || currentMethodName.equals("doCallRealMethod")
        ) {
          currentMethod = currentMethodName + "()";
        } else if (currentMethodName.equals("doAnswer") || currentMethodName.equals("thenAnswer")) {
          if (method.arguments().size() > 0) {
            String argument = method.arguments().get(0).toString();
            argument = argument.replace("\\", "\\\\").replace("\"", "\\\"");
            currentMethod = currentMethodName + "(" + argument + ")";
          }
        }

      } else if (!isVerifyMockito(method)) {

        String argument = getArgumentReturnType(method);
        currentMethod = currentMethodName + "(" + argument + ")";
      }
    }
    result = (expressionMethod + currentMethod).replace("\n", "").replace("\\", "\\\\")
      .replace("\"", "\\\"");
    return result;
  }

  static String getArgumentReturnType(MethodInvocation method) {
    List<?> arguments = method.arguments();
    StringBuilder parameterTypesBuilder = new StringBuilder();

    for (int i = 0; i < arguments.size(); i++) {
      Expression argument = (Expression) arguments.get(i);
      ITypeBinding typeBinding = argument.resolveTypeBinding();

      if (typeBinding != null) {
        parameterTypesBuilder.append(typeBinding.getBinaryName());
        if (i < arguments.size() - 1) {
          parameterTypesBuilder.append(", ");
        }
      } else {
        parameterTypesBuilder.append("UnknownType");
        if (i < arguments.size() - 1) {
          parameterTypesBuilder.append(", ");
        }
      }
    }
    return parameterTypesBuilder.toString();
  }

  static String getFormattedMethodSignature(MethodInvocation argumentMethod) {
    IMethodBinding methodBinding = argumentMethod.resolveMethodBinding();

    if (methodBinding == null) {
      return "";
    }

    // Get the name of the declared class
    String declaringClass = methodBinding.getDeclaringClass().getQualifiedName();
    String methodName = methodBinding.getName();
    StringBuilder parameters = new StringBuilder();
    ITypeBinding[] paramTypes = methodBinding.getParameterTypes();
    if (paramTypes != null) {
      for (int i = 0; i < paramTypes.length; i++) {
        parameters.append(paramTypes[i].getName());
        if (i < paramTypes.length - 1) {
          parameters.append(", ");
        }
      }
    }

    // Splicing method signature
    return declaringClass + "." + methodName + "(" + parameters.toString() + ")";
  }

  static boolean isVerifyMockito(MethodInvocation rawMethod) {
    String methodRaw = rawMethod.toString();
    boolean isVerify = false;
    Expression method = (Expression) rawMethod;
    while (method instanceof MethodInvocation) {
      isVerify = ((MethodInvocation) method).getName().getIdentifier().equals("verify");
      if (isVerify) {
        break;
      }

      method = ((MethodInvocation) method).getExpression();

    }
    return isVerify;
  }
}
