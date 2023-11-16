package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.DSLCallback;
import dsl.semanticanalysis.types.DSLContextMember;
import dsl.semanticanalysis.types.DSLType;

import java.util.function.Consumer;

@DSLType
public class TestComponentWithStringConsumerCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback private Consumer<String> onInteraction;

    public TestComponentWithStringConsumerCallback(
            @DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }

    public void executeCallbackWithText(String text) {
        onInteraction.accept(text);
    }
}