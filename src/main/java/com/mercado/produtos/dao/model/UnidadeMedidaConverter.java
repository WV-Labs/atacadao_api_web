package com.mercado.produtos.dao.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UnidadeMedidaConverter implements AttributeConverter<UnidadeMedida, String> {
  @Override
  public String convertToDatabaseColumn(UnidadeMedida attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.getCodigo();
  }

  @Override
  public UnidadeMedida convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.trim().isEmpty()) {
      return null;
    }
    return UnidadeMedida.fromCodigo(dbData.trim());
  }
}
