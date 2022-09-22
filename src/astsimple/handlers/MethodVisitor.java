package astsimple.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class MethodVisitor extends ASTVisitor{

	List<MethodDeclaration> methods = new ArrayList<>();
    
    List<MethodInvocation> methodInvocation = new ArrayList<>();
    
    @Override
    public boolean visit(MethodInvocation node) {
    	
    	methodInvocation.add(node);
    	return true;
    }
    
    public List<MethodInvocation> getMethodInvocations(){
    	return methodInvocation;
    }
    
    
    List<TypeDeclaration> TypeDeclaration = new ArrayList<>();
    
    @Override
    public boolean visit(TypeDeclaration node) {
    	
    	TypeDeclaration.add(node);
    	return true;
    }
    
    public List<TypeDeclaration> getTypeDeclarations(){
    	return TypeDeclaration;
    }
    
    

    
    
    
    
}
