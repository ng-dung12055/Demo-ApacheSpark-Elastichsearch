package com.yourname.hospital.service;

import com.yourname.hospital.dto.MasterKeyRequest;
import com.yourname.hospital.entity.KeyEntity;
import com.yourname.hospital.entity.KeyStatus;
import com.yourname.hospital.entity.KeyType;
import com.yourname.hospital.entity.KeyVault;
import com.yourname.hospital.repository.KeyEntityRepository;
import com.yourname.hospital.repository.KeyVaultRepository;
import java.util.Base64;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeyService {

    private final KeyEntityRepository keyRepository;
    private final KeyVaultRepository vaultRepository;
    private final CryptoService cryptoService;

    public KeyService(KeyEntityRepository keyRepository, KeyVaultRepository vaultRepository, CryptoService cryptoService) {
        this.keyRepository = keyRepository;
        this.vaultRepository = vaultRepository;
        this.cryptoService = cryptoService;
    }

    @Transactional
    public KeyEntity createMasterKey(MasterKeyRequest request) {
        KeyVault vault = vaultRepository.findAll().stream().findFirst()
                .orElseGet(() -> vaultRepository.save(new KeyVault(request.getStorageLocation())));
        KeyEntity active = keyRepository.findByTypeAndStatus(KeyType.MASTER, KeyStatus.ACTIVE).orElse(null);
        if (active != null) {
            active.setStatus(KeyStatus.ROTATED);
        }
        byte[] masterKey = cryptoService.generateAesKey();
        String encoded = Base64.getEncoder().encodeToString(masterKey);
        KeyEntity entity = new KeyEntity(KeyType.MASTER, encoded, vault);
        return keyRepository.save(entity);
    }

    public byte[] getActiveMasterKey() {
        KeyEntity keyEntity = keyRepository.findByTypeAndStatus(KeyType.MASTER, KeyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Master key not initialized"));
        return Base64.getDecoder().decode(keyEntity.getKeyValueEnc());
    }

    public String wrapDataKey(byte[] dataKey) {
        byte[] master = getActiveMasterKey();
        return cryptoService.encryptText(Base64.getEncoder().encodeToString(dataKey), master);
    }

    public byte[] unwrapDataKey(String wrapped) {
        byte[] master = getActiveMasterKey();
        String dataKeyBase64 = cryptoService.decryptText(wrapped, master);
        return Base64.getDecoder().decode(dataKeyBase64);
    }

    public List<KeyEntity> listMasterKeys() {
        return keyRepository.findByType(KeyType.MASTER);
    }
}
