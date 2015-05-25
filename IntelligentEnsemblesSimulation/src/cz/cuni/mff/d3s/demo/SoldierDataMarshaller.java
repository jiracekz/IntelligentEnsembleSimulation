package cz.cuni.mff.d3s.demo;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.nustaq.serialization.FSTConfiguration;

import cz.cuni.mff.d3s.deeco.annotations.pathparser.ParseException;
import cz.cuni.mff.d3s.deeco.annotations.pathparser.PathOrigin;
import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.knowledge.ValueSet;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.model.runtime.api.PathNodeField;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.KnowledgePathExt;
import cz.cuni.mff.d3s.deeco.model.runtime.impl.PathNodeFieldImpl;
import cz.cuni.mff.d3s.deeco.model.runtime.meta.RuntimeMetadataFactory;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;
import cz.cuni.mff.d3s.deeco.network.KnowledgeMetaData;
import cz.cuni.mff.d3s.deeco.task.KnowledgePathHelper;
import cz.cuni.mff.d3s.demo.components.SoldierData;
import cz.cuni.mff.d3s.jdeeco.network.marshaller.Marshaller;

public class SoldierDataMarshaller implements Marshaller {
	
	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	static KnowledgePath idKP;
	
	static final int knowledgeDataLength = Integer.BYTES + 2*Double.BYTES + Integer.BYTES + Long.BYTES;
	
	static {
		try {
			idKP = KnowledgePathHelper.createKnowledgePath("id", PathOrigin.COMPONENT);
		} catch (ParseException | AnnotationProcessorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		idKP.getNodes().set(0, RuntimeMetadataFactory.eINSTANCE.createPathNodeField());
		((PathNodeField)idKP.getNodes().get(0)).setName("id");
	}
	
	@Override
	public byte[] marshall(Object data) throws Exception {
		assert data instanceof KnowledgeData;
		KnowledgeData knowledgeData = (KnowledgeData) data;
		
		// shitty code because of DEECo design
		HashMap<String, SoldierData> everyone = (HashMap<String, SoldierData>) knowledgeData.getKnowledge().getValue(
				KnowledgePathHelper.createKnowledgePath("everyone", PathOrigin.COMPONENT));
		String id = (String) knowledgeData.getKnowledge().getValue(idKP);
		SoldierData soldierData = everyone.get(id);
		
		byte[] metadataEnc = conf.asByteArray(knowledgeData.getMetaData());
		ByteBuffer buffer = ByteBuffer.allocate(knowledgeDataLength + metadataEnc.length);
		buffer.putInt(Integer.parseInt(id));
		buffer.putDouble(soldierData.coords.getX());
		buffer.putDouble(soldierData.coords.getY());
		buffer.putInt(soldierData.ensembleId);
		buffer.putLong(soldierData.timestamp);
		buffer.put(metadataEnc);
		byte[] result = buffer.array();		
		
		return result;
	}

	@Override
	public Object unmashall(byte[] data) throws Exception {
		ValueSet knowledge = new ValueSet();
		ByteBuffer buffer = ByteBuffer.wrap(data);
		String id = Integer.toString(buffer.getInt());
		
		SoldierData soldierData = new SoldierData();
		soldierData.coords = new Coordinates(buffer.getDouble(), buffer.getDouble());
		soldierData.ensembleId = buffer.getInt();
		soldierData.timestamp = buffer.getLong();
		HashMap<String, SoldierData> everyone = new HashMap<>();
		everyone.put(id, soldierData);
		
		byte[] metadataEnc = new byte[data.length - knowledgeDataLength];
		buffer.get(metadataEnc);
		
		knowledge.setValue(idKP, id);
		knowledge.setValue(KnowledgePathHelper.createKnowledgePath("everyone", PathOrigin.COMPONENT), everyone);
		knowledge.setValue(KnowledgePathHelper.createKnowledgePath("isOnline", PathOrigin.COMPONENT), true);
		
		KnowledgeMetaData metaData = (KnowledgeMetaData) conf.asObject(metadataEnc);
		
		KnowledgeData result = new KnowledgeData(knowledge, new ValueSet(), new ValueSet(), metaData);
		
		return result;
	}

}
