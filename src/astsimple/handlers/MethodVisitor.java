package astsimple.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodVisitor extends ASTVisitor {

	List<MethodInvocation> mockito_methods = new ArrayList<>();
	List<MethodInvocation> easymock_methods = new ArrayList<>();

	List<MethodInvocation> methodInvocation = new ArrayList<>();

	@Override
	public boolean visit(MethodInvocation node) {

		methodInvocation.add(node);
		if ((node.resolveMethodBinding() != null)&&(node.resolveMethodBinding().getDeclaringClass()!= null)) {//sometimes show error with resolveMethodBinding() is null, sometimes is .getDeclaringClass()
			if (node.resolveMethodBinding().getDeclaringClass().getQualifiedName().startsWith("org.mockito")) {
				mockito_methods.add(node);
			}
			if (node.resolveMethodBinding().getDeclaringClass().getQualifiedName().startsWith("org.easymock")) {
				easymock_methods.add(node);
			}
		}

		return true;
	}

	public List<MethodInvocation> getAllMethodInvocations() {
		return methodInvocation;
	}

	public List<MethodInvocation> getMockitoMethodInvocations() {
		return mockito_methods;
	}

	public List<MethodInvocation> getEasyMockMethodInvocations() {
		return easymock_methods;
	}

}
