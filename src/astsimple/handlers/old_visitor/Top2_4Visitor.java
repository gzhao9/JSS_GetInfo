package astsimple.handlers.old_visitor;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class Top2_4Visitor extends ASTVisitor {

	List<MethodInvocation> powermock_methods = new ArrayList<>();
	List<MethodInvocation> spring_framework_list = new ArrayList<>();


	List<MethodInvocation> methodInvocation = new ArrayList<>();



	@Override
	public boolean visit(MethodInvocation node) {

//		ASTNode parentNode = Optional.ofNullable((ASTNode) parentMethodDeclaration).orElse(parentTypeDeclaration);

		methodInvocation.add(node);
		if ((node.resolveMethodBinding() != null) && (node.resolveMethodBinding().getDeclaringClass() != null)) {
			// sometimes show error with resolveMethodBinding() is null, sometimes is
			// .getDeclaringClass()

			if (node.resolveMethodBinding().getDeclaringClass().getQualifiedName().startsWith("org.powermock")) {
				powermock_methods.add(node);
//			methodInvocations.put(node, parentNode);
			}
			if (node.resolveMethodBinding().getDeclaringClass().getQualifiedName().startsWith("org.springframework")) {
				spring_framework_list.add(node);
			}
		} else {
			throw new NullPointerException();
		}

		return true;
	}



	public List<MethodInvocation> getAllMethodInvocations() {
		return methodInvocation;
	}

	public List<MethodInvocation> getPowerMockMethodInvocations() {
		return powermock_methods;
	}

	public List<MethodInvocation> getSpringFrameworkMethodInvocations() {
		return spring_framework_list;
	}

}
