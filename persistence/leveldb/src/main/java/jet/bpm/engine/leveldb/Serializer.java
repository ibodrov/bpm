package jet.bpm.engine.leveldb;

public interface Serializer {

    Object fromBytes(byte[] value);

    byte[] toBytes(Object value);
}
