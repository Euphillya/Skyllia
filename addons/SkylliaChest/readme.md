# 🌟 SkylliaChest – Addon pour Skyllia

## **Attention ! L'addon **SkylliaChest** est actuellement en BETA et peut contenir des bugs. Merci de signaler tout problème rencontré pour nous aider à améliorer l'addon.**

SkylliaChest est un addon destiné à étendre les fonctionnalités du plugin **Skyllia** en ajoutant un système de coffres d’îles personnalisés, persistant et entièrement intégré à l’écosystème Skyblock.

Ce module permet à chaque île de disposer d’un inventaire partagé, accessible via commande, interface graphique et synchronisé avec la base de données.

---

## 📦 Fonctionnalités

* 🔒 **Coffre d’île partagé**
<p> Un inventaire propre à chaque île, disponible pour tous les membres (s'ils ont la permission donnée par le OWNER).

* 💾 **Persistance des données**
<p> Les données des coffres sont stockées dans la base de données de Skyllia, avec un système de cache pour optimiser les performances.

* ⚡ **Chargement asynchrone**
<p> Les données sont chargées et sauvegardées de manière asynchrone pour éviter les blocages du serveur.


---

## 🛠️ Commandes

| Commande    | Description                      |
|-------------|----------------------------------|
| `/is chest` | Ouvre le coffre partagé de l’île |


---

## 🔧 API (pour les développeurs)

Exemple pour récupérer le coffre d’une île :

```java
ChestIsland chest = ChestIslandCache.getChestIsland(islandId);
if (chest != null) {
    Inventory inv = chest.getInventory();
}
```
