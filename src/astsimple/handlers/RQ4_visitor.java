package astsimple.handlers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class RQ4_visitor extends ASTVisitor {
	List<String> have_mock = new ArrayList<>();

	@Override
	public boolean visit(MethodInvocation node) {
		String node_name = node.getName().toString();
		if (node_name.contains("mock")) {
			have_mock.add(node_name + ",mock");

		} else if (node_name.contains("spy")) {
			have_mock.add(node_name + ",spy");
		}
		return true;
	}

	public List<String> getContainMock() {
		return have_mock;
	}
}