package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.*;

public class OverrideRatioCalculator extends MetricValueCalculator {
	
	private Double computeOverrideRatio(IMethodBinding[] localMethods, IMethodBinding[] superclassMethods) {
		if (superclassMethods == null || superclassMethods.length == 0) {
			return null;
		}
		double methodsOverridden = 0d;
		for (IMethodBinding localMethod : localMethods) {
			for (IMethodBinding superMethod : superclassMethods) {
				if (localMethod.overrides(superMethod)) {
					methodsOverridden++;
					break;
				}
			}
		}
		return methodsOverridden / superclassMethods.length;
	}
	
	@Override
	protected Double computeValue(ASTNode target) {
		TypeDeclaration typeDeclaration = (TypeDeclaration)target;
		IBinding binding = typeDeclaration.resolveBinding();
		if (binding != null && binding.getKind() == IBinding.TYPE) {
			ITypeBinding typeBinding = (ITypeBinding)binding;
			IMethodBinding[] localMethods = typeBinding.getDeclaredMethods(); 

			ITypeBinding superClass = typeBinding.getSuperclass();
			if (superClass == null) {
				return null;
			}
			IMethodBinding[] superclassMethods = superClass.getDeclaredMethods();
			
			/**
			 * Only considers this metric if the target has a superclass
			 * different from java.lang.Object
			 */
			if (!superClass.getQualifiedName().equals(Object.class.getName())){
				return computeOverrideRatio(localMethods, superclassMethods);
			}
		}
		return null;
	}

	@Override
	public MetricName getMetricName() {
		return MetricName.OverrideRatio;
	}

}
