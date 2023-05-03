package astsimple.handlers;

import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class MockedObjectVisitor extends ASTVisitor {
	private ArrayList<MockedObject> mockedObjects = new ArrayList<>();

	public ArrayList<MockedObject> getMockedObjects() {
		return mockedObjects;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (Object fragment : node.fragments()) {
			if (fragment instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment varFrag = (VariableDeclarationFragment) fragment;
				if (varFrag.getInitializer() instanceof MethodInvocation) {
					MethodInvocation methodInv = (MethodInvocation) varFrag.getInitializer();
					if (methodInv.getName().getIdentifier().equals("mock")) {
						MockedObject mockedObject = new MockedObject();
						mockedObject.setName(varFrag.getName().getIdentifier());
						mockedObject.setMockedClass(methodInv.arguments().get(0).toString());
						mockedObject.setMethod("mock");
						mockedObjects.add(mockedObject);
					}
					if (methodInv.getName().getIdentifier().equals("spy")) {
						MockedObject mockedObject = new MockedObject();
						mockedObject.setName(varFrag.getName().getIdentifier());
						mockedObject.setMockedClass(methodInv.arguments().get(0).toString());
						mockedObject.setMethod("spy");
						mockedObjects.add(mockedObject);
					}
				}
			}
		}
		return true;
	}

	public class MockedObject {
		private String name;
		private String mockedClass;
		private String method;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMockedClass() {
			return mockedClass;
		}

		public void setMockedClass(String mockedClass) {
			this.mockedClass = mockedClass;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}
	}
}
