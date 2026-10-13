package com.pulsepass.organizationservice.unit.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.organizationservice.common.JsonHandlers;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonHandlersTest {

    @Test
    void toSnakeCase_nestedPathWithIndex_convertsEverySegment() {
        assertThat(JsonHandlers.toSnakeCase("legalPersonCollaborators[0].documentNumber"))
                .isEqualTo("legal_person_collaborators[0].document_number");
    }

    @Test
    void toSnakeCase_singleWord_keepsValue() {
        assertThat(JsonHandlers.toSnakeCase("name")).isEqualTo("name");
    }

    @Test
    void toJson_map_serializesKeysAsGiven() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("organization_key", "abc");
        data.put("balance_amount", 0);

        assertThat(JsonHandlers.toJson(data)).isEqualTo("{\"organization_key\":\"abc\",\"balance_amount\":0}");
    }

    @Test
    void toJson_pojo_usesSnakeCaseNaming() {
        assertThat(JsonHandlers.toJson(new Sample("x"))).isEqualTo("{\"first_name\":\"x\"}");
    }

    @Test
    void toJson_selfReferencingObject_throwsIllegalArgument() {
        Map<String, Object> loop = new LinkedHashMap<>();
        loop.put("self", loop);

        assertThatThrownBy(() -> JsonHandlers.toJson(loop)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_utilityClass_isPrivate() throws Exception {
        var constructor = JsonHandlers.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThat(constructor.newInstance()).isNotNull();
    }

    public record Sample(String firstName) {
    }
}
