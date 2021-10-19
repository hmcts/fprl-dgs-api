provider "azurerm" {
    features {}
}

locals {
  aseName = "core-compute-${var.env}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

  idam_s2s_url                       = "http://${var.idam_s2s_url_prefix}-${local.local_env}.service.core-compute-${local.local_env}.internal"

  previewVaultName = "${var.reform_team}-aat"
  nonPreviewVaultName = "${var.reform_team}-${var.env}"
  vaultName = "${var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName}"
  vaultUri = data.azurerm_key_vault.fprl_key_vault.vault_uri

  asp_name = "${var.env == "prod" ? "fprl-dgs-prod" : "${var.raw_product}-${var.env}"}"
  asp_rg = "${var.env == "prod" ? "fprl-dgs-prod" : "${var.raw_product}-${var.env}"}"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location
}

data "azurerm_key_vault" "fprl_key_vault" {
    name                = local.vaultName
    resource_group_name = local.vaultName
}

data "azurerm_key_vault_secret" "idam-secret" {
    name      = "idam-secret"
    key_vault_id = data.azurerm_key_vault.fprl_key_vault.id
}
