package de.fhg.iais.roberta.ast.syntax.actors;

import org.junit.Test;

import de.fhg.iais.roberta.testutil.Helper;

public class ToneActionTest {
    @Test
    public void playTone() throws Exception {
        final String a = "tone(9,300, 100);";

        Helper.assertCodeIsOk(a, "/ast/actions/action_PlaySound.xml");
    }
}
