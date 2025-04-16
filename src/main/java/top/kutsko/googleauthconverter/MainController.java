package top.kutsko.googleauthconverter;

import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import top.kutsko.googleauthconverter.model.ConvertRequest;
import top.kutsko.googleauthconverter.model.OtpMigration;
import top.kutsko.googleauthconverter.model.OtpParametersResult;

@Controller
@RequiredArgsConstructor
public class MainController {

    @GetMapping("/")
    public Mono<Rendering> index() {
        return Mono.just(Rendering.view("index").build());
    }

    @PostMapping("/convert")
    public Mono<Rendering> convert(@ModelAttribute ConvertRequest request, Model model) {
        var code = request.getCode();
        var decodedData = URLDecoder.decode(code, StandardCharsets.UTF_8);
        var protobuf = Base64.getDecoder().decode(decodedData);
        OtpMigration.MigrationPayload migrationPayload;
        try {
            migrationPayload = OtpMigration.MigrationPayload.parseFrom(protobuf);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }

        var base32 = new Base32();
        List<OtpParametersResult> results = migrationPayload.getOtpParametersList().stream()
            .map(it -> {
                var secret = base32.encodeAsString(it.getSecret().toByteArray());
                return new OtpParametersResult(secret, it.getName(), it.getIssuer());
            })
            .toList();

        model.addAttribute("results", results);
        return Mono.just(Rendering.view("index :: result").build());
    }
}
