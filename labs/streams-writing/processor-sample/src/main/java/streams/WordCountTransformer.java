package streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;

public class WordCountTransformer implements Transformer<String, String, KeyValue<String, Long>> {

  private KeyValueStore<String, Long> kvStore;

  @Override
  @SuppressWarnings("unchecked")
  public void init(final ProcessorContext context) {
    context.schedule(
        Duration.ofMillis(5_000),
        PunctuationType.STREAM_TIME,
        //iterates through the entries in the key-value store at that particular time(stamp) and prints it out
        timestamp -> {
          KeyValueIterator<String, Long> iter = kvStore.all();
          System.out.println("------ " + context.taskId() + " - " + timestamp + " -----" + " ");
          while (iter.hasNext()) {
            KeyValue<String, Long> entry = iter.next();
            System.out.println("[" + entry.key + ", " + entry.value + "]");
            context.forward(entry.key, entry.value);
          }
        });
    this.kvStore = (KeyValueStore<String, Long>) context.getStateStore("Counts");
  }

  @Override
  public KeyValue<String, Long> transform(String word, String dummy) {
    // TODO: Get the correct entry from the keystore and update it or create an intial entry
      //create initial entry if absent.
      this.kvStore.putIfAbsent(word, 0L);
      this.kvStore.put(word, this.kvStore.get(word) + 1);
      return new KeyValue<>(word, this.kvStore.get(word));
  }

  @Override
  public void close() {
      KeyValueIterator<String, Long> iter = this.kvStore.all();
      while(iter.hasNext())
        this.kvStore.delete(iter.next().key);
  }
}
