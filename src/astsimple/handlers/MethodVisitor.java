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

	@Override
    public boolean visit(MethodDeclaration node) {
        methods.add(node);
//        return super.visit(node);
        return true;
    }

    public List<MethodDeclaration> getMethods() {
        return methods;
    }
    
    
    List<MethodInvocation> methodInvocation = new ArrayList<>();
    
    @Override
    public boolean visit(MethodInvocation node) {
    	
    	methodInvocation.add(node);
//    	System.out.println(node);
//    	return super.visit(node);
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
    
    
    
    List<ClassInstanceCreation> ClassCreation = new ArrayList<>();
    
    @Override
    public boolean visit(ClassInstanceCreation node) {
    	
    	ClassCreation.add(node);
//    	return super.visit(node);
    	return true;
    }
    
    public List<ClassInstanceCreation> getClassCreation(){
    	return ClassCreation;
    }
    
    
    
    
    
}
