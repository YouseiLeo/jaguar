package br.usp.each.saeg.jaguar.core.results;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.xml.bind.JAXB;

import br.usp.each.saeg.jaguar.codeforest.model.Class;
import br.usp.each.saeg.jaguar.codeforest.model.FaultClassification;
import br.usp.each.saeg.jaguar.codeforest.model.Method;
import br.usp.each.saeg.jaguar.codeforest.model.Package;
import br.usp.each.saeg.jaguar.codeforest.model.Requirement;
import br.usp.each.saeg.jaguar.codeforest.model.SuspiciousElement;
import br.usp.each.saeg.jaguar.core.infra.FileUtils;
import br.usp.each.saeg.jaguar.core.results.model.FaultLocalizationReport;

public class Report {
	
	private static final String DEFUALT_RESULT_FILE = "\\JaguarReport.xml";
	private static final String DEFAULT_FOLDER = ".\\jaguar-results\\";

	public void createReport (final File folder, final File reportFile) throws FileNotFoundException{
		
		SuspiciousElement faultyElement = getFault(folder);
		List<FaultClassification> jaguarFileList = getJaguarFiles(folder, reportFile);
		
		Summarizer summarizer = new Summarizer(jaguarFileList, faultyElement);
		FaultLocalizationReport faultLocalizationReport = summarizer.summarizePerformResults();
		
		JAXB.marshal(faultLocalizationReport, reportFile);
		
	}

	/**
	 * Extract the FaultClassification object of each Xml file inside the given folder.
	 * Except for fault.xml and the file with this report output name.
	 * 
	 * @param folder The fodler to be searched.
	 * @param reportFile The current report output file.
	 * @return A list of FaultLocalization objects.
	 */
	private List<FaultClassification> getJaguarFiles(final File folder,
			final File reportFile) {
		
		List<File> resultFiles = FileUtils.findFilesEndingWith(folder,new String[] { ".xml" });
		resultFiles.removeIf(new Predicate<File>() {
			@Override
			public boolean test(File t) {
				return (t.getName().equals("fault.xml") || t.getName().equals(reportFile.getName()));
			}
			
		});
		
		List<FaultClassification> jaguarFileList = new ArrayList<FaultClassification>();
		for (File jaguarFile : resultFiles) {
			jaguarFileList.add( JAXB.unmarshal(jaguarFile,FaultClassification.class));
		}
		return jaguarFileList;
	}
	
	public static List<SuspiciousElement> extractElementsFromPackages(Collection<Package> packages) {
		List<SuspiciousElement> elements = new ArrayList<SuspiciousElement>();
		for (Package currentPackage : packages) {
			Collection<Class> classes = currentPackage.getClasses();
			for (Class currentClass : classes) {
				Collection<Method> methods = currentClass.getMethods();
				for (Method currentMethod : methods) {
					Collection<Requirement> requirements = currentMethod.getRequirements();
					for (Requirement requirement : requirements) {
						requirement.setName(currentClass.getName() + "." + currentMethod.getName());
						elements.add(requirement);
					}
				}
			}
		}
		return elements;
	}

	/**
	 * 
	 * Extract the suspiciousElement from the fault.xml file inside the given folder.
	 * 
	 * @param folder the folder that contains the fault.xml file.
	 * @return the faulty element 
	 * @throws FileNotFoundException when there is no file named fault.xml
	 */
	private SuspiciousElement getFault(File folder) throws FileNotFoundException {
		File faultFile = FileUtils.getFile(folder, "fault.xml");
		if (faultFile == null) {
			throw new FileNotFoundException(
					"No file named 'fault.xml was found");
		} else {
			Package faultPackage =  JAXB.unmarshal(faultFile, Package.class);
			return extractElementsFromPackages(Collections.singleton(faultPackage)).iterator().next();
		}
	}

	/**
	 * First arg (arg[0]) is the folder path that contains the xml generated by Jaguar.
	 * Second arg (arg[1]) is the name of the result File.
	 * 
	 * If the first arg is not present, it is used the default = .\jaguar-results\
	 * If the second arg is not present, it is used the default =  {arg0}\JaguarReport.xml
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		File folder = args.length > 0 ? new File(args[0]) : new File(DEFAULT_FOLDER);
		File resultFile = args.length > 1 ? new File(args[1]) : new File(folder.getAbsolutePath() + DEFUALT_RESULT_FILE);
		new Report().createReport(folder, resultFile);
	}

}