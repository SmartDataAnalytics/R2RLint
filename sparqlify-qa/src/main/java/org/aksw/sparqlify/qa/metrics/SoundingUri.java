package org.aksw.sparqlify.qa.metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;

/**
 * The SoundingUri metric checks if a given URL has a sounding name i.e. if it
 * is built up of
 * - a sounding host part
 * - sounding path segments
 * - sounding query parameters and fragment determiner
 * 
 * The first targeted approach was to use phonotactic rules of the English
 * language and check, if the parts of the URL of a given resource conform with
 * these rules and so find out if these parts at least sound like English words.
 * 
 * Looking up a dictionary would probably not make that much sense, since words
 * are often abbreviated in URLs or URL parts are build up concatenating more
 * than one word.
 * The problem was to find persisted phonotactic rules provided under a free
 * license. Creating these rules via a learning approach was not suitable since
 * using libraries e.g. implementing the Java Speech API ended up in a lot of
 * overhead just for the phonemization of the training data and the URL parts
 * that should be assessed.
 * Other approaches using Bayesian filters, Markov chains or simple probability
 * distributions were tried out but they didn't fit right or were too complex.
 * 
 * So in the end a trigram based approach was applied using trigram statistics
 * of the English language learned from randomly chosen Wikipedia articles,
 * namely:
 * 
 * - http://en.wikipedia.org/wiki/English_language
 * - http://en.wikipedia.org/wiki/Anna_Anderson
 * - http://en.wikipedia.org/wiki/Daylight_saving_time
 * 
 * The words of these articles were extracted using Python:
 * 
 * >>> file = open('/tmp/enwiki_dst.txt')
 * >>> content = file.read()
 * >>> raw_words = content.split()
 * >>> raw_words[:20]
 * ['Daylight', 'saving', 'time', 'From', 'Wikipedia,', 'the', 'free', ...]
 * >>> words = [ word.lower() for word in raw_words if word.isalpha() ]
 * >>> words[:20]
 * ['daylight', 'saving', 'time', 'from', 'the', 'free', 'encyclopedia', ...]
 * >>> out_file = open('words3.txt', 'w')
 * >>> for word in words:
 * ...   foo = out_file.write(word + '\n')
 * ...
 * >>>
 * 
 * After that all the word lists were concatenated and duplicates were
 * removed (sort words.txt | uniq > uwords.txt).
 * This word list file is used to set up a HashMap containing the trigram
 * statistics. So to improve these statistics or adjust them to another language
 * one can use another word list file containing representational words.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class SoundingUri extends PinpointMetric implements NodeMetric {

	HashMap<String, Integer> trigramStats;
	int numTrigrams;
	// TODO: factor chosen arbitrarily
	int scoreFactor = 1000;
	
	
	public SoundingUri() throws IOException {
		String filePath = "src/main/resources/uwords_all.txt";
		trigramStats = new HashMap<String, Integer>();
		numTrigrams = 0;
		
		FileReader fReader = new FileReader(filePath);
		BufferedReader bReader = new BufferedReader(fReader);
		
		String lineBuff = "";

		while ((lineBuff = bReader.readLine() ) != null) {
			updateTrigramStats(lineBuff);
		}
		
		bReader.close();
		fReader.close();
	}
	
	
	private void updateTrigramStats(String line) {
		int strLength = line.length();
			
		if (strLength == 3) {
			Integer currVal = trigramStats.get(line);
			
			if (currVal != null) trigramStats.put(line, currVal++);
			else trigramStats.put(line, 1);
			
			numTrigrams++;
			
		} else if (strLength > 3 ){
			for (int indx=0; indx<=strLength-3; indx++) {
				String trigram = line.substring(indx, indx + 3);
				Integer currVal = trigramStats.get(trigram);
				
				if (currVal != null){
					currVal++;
					trigramStats.put(trigram, currVal);
				
				} else trigramStats.put(trigram, 1);
				
				numTrigrams++;
			}
		}
	}
	
	
	@Override
	public void assessNodes(Triple triple) throws NotImplementedException {
		/* assess subject */
		Node subj = triple.getSubject();
		
		if (subj.isURI()) {
			float qualityVal = assessResource((Node_URI) subj);
			
			if (qualityVal < threshold) {
				Set<ViewQuad<ViewDefinition>> viewQuads =
									pinpointer.getViewCandidates(triple);
			
				writeToSink(qualityVal, TriplePosition.SUBJECT, triple, viewQuads);
			}
		}
		
		/* assess predicate */
		{
			Node pred = triple.getPredicate();
			
			float qualityVal = assessResource((Node_URI) pred);
			
			if (qualityVal < threshold) {
				Set<ViewQuad<ViewDefinition>> viewQuads =
									pinpointer.getViewCandidates(triple);
			
				writeToSink(qualityVal, TriplePosition.PREDICATE, triple, viewQuads);
			}
		}

		/* assess object */
		Node obj = triple.getObject();
		
		if (obj.isURI()) {
			float qualityVal = assessResource((Node_URI) obj);
			
			if (qualityVal < threshold) {
				Set<ViewQuad<ViewDefinition>> viewQuads =
									pinpointer.getViewCandidates(triple);
			
				writeToSink(qualityVal, TriplePosition.OBJECT, triple, viewQuads);
			}
		}
	}

	
	private float assessResource(Node_URI res) {
		float score = 0;
		int numAssessedTrigrams = 0;
		
		String uri = res.getURI();
		String[] parts = uri.split("/");
		
		if (parts.length < 2) {
			// broken URI assumed
			return 0;
			
		} else {
			/* 
			 * e.g.:
			 * parts
			 * (java.lang.String[]) [http:, , ex.org, employee, Chaplin, Charly]
			 */
			
			// starting with partNum=2 means, that the protocol prefix and the
			// empty tring (resulting from "http://ex.org....".split("/")) are
			// not considered here
			for (int partNum=2; partNum<parts.length; partNum++) {
				String part = parts[partNum];
				
				int partLength = part.length();
				
				// applying trigram comparison makes no sense here
				if (partLength < 3) continue;
				
				else {
					for (int idx=0; idx<=partLength-3; idx++){
						
						String trigram = part.substring(idx, idx+3).toLowerCase();
						Integer trigramCount = trigramStats.get(trigram);
						
						if (trigramCount == null) numAssessedTrigrams++;
						
						else {
							float trigramScore = trigramCount*scoreFactor/(float) numTrigrams;
							score += trigramScore;
							numAssessedTrigrams ++;
						}
					}
				}
			}
		}
		return score/(float) numAssessedTrigrams;
	}
}
