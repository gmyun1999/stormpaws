package com.example.stormpaws.service.oauth;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OAuthProviderFactory {

  private final Map<String, IOAuthProvider> providerMap;

  public OAuthProviderFactory(List<IOAuthProvider> providers) {
    this.providerMap =
        providers.stream()
            .collect(
                Collectors.toMap(
                    provider -> provider.getProviderName().toLowerCase(), provider -> provider));
  }

  public IOAuthProvider getProvider(String authServer) {
    IOAuthProvider provider = providerMap.get(authServer.toLowerCase());
    if (provider == null) {
      throw new IllegalArgumentException("Unsupported auth server: " + authServer);
    }
    return provider;
  }
}
