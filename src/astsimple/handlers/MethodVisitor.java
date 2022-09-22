package astsimple.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodVisitor extends ASTVisitor {

	List<MethodInvocation> mockito_methods = new ArrayList<>();
	List<MethodInvocation> easymock_methods = new ArrayList<>();

	List<MethodInvocation> methodInvocation = new ArrayList<>();

	@Override
	public boolean visit(MethodInvocation node) {

		methodInvocation.add(node);
		IMethodBinding binding = node.resolveMethodBinding();
		if ((binding != null)&&(binding.getDeclaringClass()!= null)) {//sometimes show error with resolveMethodBinding() is null, sometimes is .getDeclaringClass()
			if (binding.getDeclaringClass().getQualifiedName().startsWith("org.mockito")) {
				mockito_methods.add(node);
			}
			if (binding.getDeclaringClass().getQualifiedName().startsWith("org.easymock")) {
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
