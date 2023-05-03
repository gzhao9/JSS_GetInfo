package astsimple.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import astsimple.handlers.MockedObjectVisitor.MockedObject;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class TestCaseObjectVisitor_copy extends ASTVisitor {
	private String currentTestCaseName;
	private Map<String, String> typeToTestCaseMap = new HashMap<>();
	private Map<String, String> typeToMockMap = new HashMap<>();
	private Map<String, String> methodToTestCaseMap = new HashMap<>();

	private Map<String, String> methodToMockMap = new HashMap<>();

	public Map<String, String> getTypeToTestCaseMap() {
		return typeToTestCaseMap;
	}

	public Map<String, String> getTypeToMockMap() {
		return typeToMockMap;
	}

	public Map<String, String> getmethodToTestCaseMap() {
		return methodToTestCaseMap;
	}

	public Map<String, String> getmethodToMockMap() {
		return methodToMockMap;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		// Update currentTestCaseName whenever a new test case is encountered
		currentTestCaseName = node.getName().getFullyQualifiedName();
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// Update currentTestCaseName whenever a new test method is encountered
		currentTestCaseName = node.getName().getFullyQualifiedName();
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		// If the method calls a constructor, record the class of the object it creates
		// and the name of the test case it belongs to
		if (node.getName().getIdentifier().equals("<init>")) {
			ITypeBinding createdType = node.resolveMethodBinding().getDeclaringClass();
			typeToTestCaseMap.put(createdType.getQualifiedName(), currentTestCaseName);
			if (!methodToMockMap.containsKey(createdType.getQualifiedName())) {
				typeToMockMap.put(createdType.getQualifiedName(), isMock(node));
			}
		}

		// get whether the method is mock
		if ((node.resolveMethodBinding() != null) && (node.resolveMethodBinding().getDeclaringClass() != null)) {
			if (node.getName().getIdentifier().equals("when")) {
//			if (node.resolveMethodBinding().getDeclaringClass().getQualifiedName().startsWith("org.mockito.Mockito.when")) {
				if (node.arguments().get(0) instanceof MethodInvocation) {
					MethodInvocation metodStubed = (MethodInvocation) node.arguments().get(0);
					String methodName = metodStubed.getName().getIdentifier();
					String declaringClassName = metodStubed.resolveMethodBinding().getDeclaringClass()
							.getQualifiedName();
					String fullMethodName = declaringClassName + "." + methodName;
					methodToTestCaseMap.put(fullMethodName, currentTestCaseName);

					methodToMockMap.put(fullMethodName, "stubbed");
				}
			} else if (node.getName().getIdentifier().equals("mock") || node.getName().getIdentifier().equals("spy")
					|| node.getName().getIdentifier().equals("mockStatic")) {

				MethodInvocation methodInv = node;
				Object arg = methodInv.arguments().get(0);
				if (arg instanceof TypeLiteral) {
					Type type = ((TypeLiteral) arg).getType();
					if (type.isParameterizedType()) {
						type = ((ParameterizedType) type).getType();
					}
					ITypeBinding typeBinding = type.resolveBinding();
					if (typeBinding != null && typeBinding.isClass()) {
						String typeName = typeBinding.getQualifiedName();
//							System.out.println(typeName);
						typeToTestCaseMap.put(typeName, currentTestCaseName);
						if (methodInv.getName().getIdentifier().equals("mock")) {
							typeToMockMap.put(typeName, "mock");
						}
						if (methodInv.getName().getIdentifier().equals("spy")) {
							typeToMockMap.put(typeName, "spy");
						}
						if (methodInv.getName().getIdentifier().equals("mockStatic")) {
							typeToMockMap.put(typeName, "mockStatic");
						}

					}
				}

			} else {
				String methodName = node.getName().getIdentifier();
				String declaringClassName = node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
				String fullMethodName = declaringClassName + "." + methodName;

				methodToTestCaseMap.put(fullMethodName, currentTestCaseName);
				if ((!methodToMockMap.containsKey(fullMethodName)) && (!(fullMethodName.contains("mockito")))) {
					methodToMockMap.put(fullMethodName, "not-stubbed");
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		// Record the class to which the created object belongs and the name of the test
		// case to which it belongs

		ITypeBinding createdType = node.resolveTypeBinding();

		typeToTestCaseMap.put(createdType.getQualifiedName(), currentTestCaseName);
		if (!methodToMockMap.containsKey(createdType.getQualifiedName())) {
			typeToMockMap.put(createdType.getQualifiedName(), isMock(node));
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
//	Store the mock created with the annotation
		for (Object x : node.modifiers()) {
			if (x instanceof Annotation) {
				Annotation annotation = (Annotation) x;

				if (node.getType().resolveBinding() != null
						&& annotation.resolveTypeBinding().getQualifiedName().toLowerCase().contains("mockito.Mock")) {

					typeToTestCaseMap.put(node.getType().resolveBinding().getQualifiedName(), currentTestCaseName);
					typeToMockMap.put(node.getType().resolveBinding().getQualifiedName(), "mock");
				}

			}
		}
		return true;
	}

	private String isMock(MethodInvocation node) {
		// Determine if it is a mock method of Mockito
		if (node.resolveMethodBinding().getDeclaringClass().getQualifiedName().startsWith("org.mockito.Mockito.mock")) {
			return "mock";
		} else if (node.resolveMethodBinding().getDeclaringClass().getQualifiedName()
				.startsWith("org.mockito.Mockito.spy")) {
			return "spy";
		}
		return "Instantiated";
	}

	private String isMock(ClassInstanceCreation node) {
		// Determine if it is a mock method of Mockito
		if (node.getType().toString().startsWith("org.mockito.Mockito.mock")) {
			return "mock";
		}
		if (node.getType().toString().startsWith("org.mockito.Mockito.spy")) {
			return "spy";
		}
		return "Instantiated";
	}

}
