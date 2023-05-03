package astsimple.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import astsimple.handlers.MockedObjectVisitor.MockedObject;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class TestCaseObjectVisitor extends ASTVisitor {
	class TestCaseObject {
		public ArrayList<String> annotations = new ArrayList<>();
		public Map<String, String> method_recording = new HashMap<>();;
		public Map<String, String> object_recording = new HashMap<>();;
	}

	private String currentTestCaseName;
	private Map<String, TestCaseObject> TestCaseRecord = new HashMap<>();

	public Map<String, TestCaseObject> getTestCaseRecord() {

		return TestCaseRecord;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		// Update currentTestCaseName whenever a new test case is encountered
		currentTestCaseName = node.getName().getFullyQualifiedName();
		if (TestCaseRecord.get(currentTestCaseName) == null) {
			TestCaseRecord.put(currentTestCaseName, new TestCaseObject());
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// Update currentTestCaseName whenever a new test method is encountered

		currentTestCaseName = node.getName().getFullyQualifiedName();
		if (TestCaseRecord.get(currentTestCaseName) == null) {
			TestCaseRecord.put(currentTestCaseName, new TestCaseObject());
		}

		List<?> modifiers = node.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				if (!TestCaseRecord.get(currentTestCaseName).annotations.contains(((Annotation) modifier).toString())) {
					String annotation = ((Annotation) modifier).toString();
					TestCaseRecord.get(currentTestCaseName).annotations.add(annotation);
				}
			}
		}

		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		// If the method calls a constructor, record the class of the object it creates
		// and the name of the test case it belongs to
		if (TestCaseRecord.get(currentTestCaseName) == null) {
			TestCaseRecord.put(currentTestCaseName, new TestCaseObject());
		}

		if ((node.resolveMethodBinding() != null) && (node.resolveMethodBinding().getDeclaringClass() != null)) {
			String methodName = node.getName().getIdentifier();
			String declaringClassName = node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
			String fullMethodName = declaringClassName + "." + methodName;

			// get whether the method is mock
			if (methodName.equals("<init>")) {
				ITypeBinding createdType = node.resolveMethodBinding().getDeclaringClass();

				if (!TestCaseRecord.get(currentTestCaseName).object_recording
						.containsKey(createdType.getQualifiedName())) {
					TestCaseRecord.get(currentTestCaseName).object_recording.put(createdType.getQualifiedName(),
							fullMethodName);
				}
				return true;
			}

			if (methodName.equals("when")) {
//				if (node.resolveMethodBinding().getDeclaringClass().getQualifiedName().startsWith("org.mockito.Mockito.when")) {
				if (node.arguments().get(0) instanceof MethodInvocation) {
					MethodInvocation metodStubed = (MethodInvocation) node.arguments().get(0);
					methodName = metodStubed.getName().getIdentifier();
					declaringClassName = metodStubed.resolveMethodBinding().getDeclaringClass().getQualifiedName();
					fullMethodName = declaringClassName + "." + methodName;

//					methodToTestCaseMap.put(fullMethodName, currentTestCaseName);

					TestCaseRecord.get(currentTestCaseName).method_recording.put(fullMethodName, "stubbed");
				}
				return true;
			}
			if (methodName.equals("mock") || methodName.equals("spy") || methodName.equals("mockStatic")) {

				Object arg = node.arguments().get(0);
				if (arg instanceof TypeLiteral) {
					Type type = ((TypeLiteral) arg).getType();
					if (type.isParameterizedType()) {
						type = ((ParameterizedType) type).getType();
						ITypeBinding typeBinding = type.resolveBinding();
						if (typeBinding != null && typeBinding.isClass()) {
							String typeName = typeBinding.getQualifiedName();
							TestCaseRecord.get(currentTestCaseName).object_recording.put(typeName, fullMethodName);
						}
					}
				}
				return true;
			}

			// get whether the method is mock
			if (!TestCaseRecord.get(currentTestCaseName).method_recording.containsKey(fullMethodName)) {
				TestCaseRecord.get(currentTestCaseName).method_recording.put(fullMethodName, "not-stubbed");
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		// Record the class to which the created object belongs and the name of the test
		// case to which it belongs

		ITypeBinding createdType = node.resolveTypeBinding();
		if (!TestCaseRecord.get(currentTestCaseName).object_recording.containsKey(createdType.getQualifiedName())) {
			TestCaseRecord.get(currentTestCaseName).object_recording.put(createdType.getQualifiedName(), isMock(node));
		}
		return true;
	}

	// Accessing assignment expression nodes
	@Override
	public boolean visit(Assignment assignment) {
		if (assignment.getLeftHandSide() instanceof SimpleName) {

			if ((assignment.getLeftHandSide().resolveTypeBinding() != null)
					&& (assignment.getLeftHandSide().resolveTypeBinding().getPackage() != null)) {
				Expression leftHandSide = assignment.getLeftHandSide();

				// Get the type of the left-hand expression
				ITypeBinding typeBinding = leftHandSide.resolveTypeBinding();

				// Get the name of the package to which the type belongs
				String objectName = typeBinding.getPackage().getName();

				Expression rightHandSide = assignment.getRightHandSide();
				if (rightHandSide instanceof MethodInvocation) {
					// If the right-hand side of the assignment is a method call expression, get its
					// method name and package name
					MethodInvocation node = (MethodInvocation) rightHandSide;
					if ((node.resolveMethodBinding() != null)
							&& (node.resolveMethodBinding().getDeclaringClass() != null)) {
						String methodName = node.getName().getIdentifier();
						String declaringClassName = node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
						String fullMethodName = declaringClassName + "." + methodName;

						if (!TestCaseRecord.get(currentTestCaseName).object_recording.containsKey(objectName)) {
							TestCaseRecord.get(currentTestCaseName).object_recording.put(objectName, fullMethodName);
						}
					}

				}
			}

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
					String objectName = node.getType().resolveBinding().getQualifiedName();
					if (!TestCaseRecord.get(currentTestCaseName).object_recording.containsKey(objectName)) {
						TestCaseRecord.get(currentTestCaseName).object_recording.put(objectName,
								"org.mockito.Mockito.mock");
					}
//					typeToTestCaseMap.put(node.getType().resolveBinding().getQualifiedName(), currentTestCaseName);
//					typeToMockMap.put(node.getType().resolveBinding().getQualifiedName(), "org.mockito.Mockito.mock");
				}

			}
		}
//		
		// Get the type and name of the variable on the left
		if (node.getType().resolveBinding() != null) {
			String objectName = node.getType().resolveBinding().getQualifiedName();

			// Get the function call on the right
			Expression expression = ((VariableDeclarationFragment) node.fragments().get(0)).getInitializer();
			if (expression instanceof MethodInvocation) {
				MethodInvocation methodInvocation = (MethodInvocation) expression;
				// Get the method name and package name of the function call
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				if (methodBinding != null) {
					String methodName = methodInvocation.getName().getIdentifier();
					ITypeBinding declaringClass = methodBinding.getDeclaringClass();
					if (declaringClass != null && declaringClass.getPackage() != null) {
						String packageName = declaringClass.getPackage().getName() + '.' + declaringClass.getName();

						String fullMethodName = packageName + '.' + methodName;
						if (!TestCaseRecord.get(currentTestCaseName).object_recording.containsKey(objectName)) {
							TestCaseRecord.get(currentTestCaseName).object_recording.put(objectName, fullMethodName);
						}
					}
				}
			}
		}
		return true;
	}

	public boolean visit(VariableDeclarationStatement node) {
		// Get the type and name of the variable on the left
		if (node.getType().resolveBinding() != null) {
			String objectName = node.getType().resolveBinding().getQualifiedName();

			// Get the function call on the right
			Expression expression = ((VariableDeclarationFragment) node.fragments().get(0)).getInitializer();
			if (expression instanceof MethodInvocation) {
				MethodInvocation methodInvocation = (MethodInvocation) expression;
				// Get the method name and package name of the function call
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				if (methodBinding != null) {
					String methodName = methodInvocation.getName().getIdentifier();
					ITypeBinding declaringClass = methodBinding.getDeclaringClass();
					if (declaringClass != null && declaringClass.getPackage() != null) {
						String packageName = declaringClass.getPackage().getName() + '.' + declaringClass.getName();

						String fullMethodName = packageName + '.' + methodName;
						if (!TestCaseRecord.get(currentTestCaseName).object_recording.containsKey(objectName)) {
							TestCaseRecord.get(currentTestCaseName).object_recording.put(objectName, fullMethodName);
						}
					}
				}
			}
		}
		return true;
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
