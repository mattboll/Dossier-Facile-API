![Republique Francaise](src/main/resources/img/Republique-Francaise.png "Logo République Française")

# Service de vérification de justificatifs d'identité
## Présentation du service

Le service de vérification permet de vérifier les justificatifs d'identité sur deux éléments :
1. L'authenticité de la signature
2. La validité du cadre d'usage

## Comment utiliser l'image Docker

Pour utiliser le service de vérification, il est possible de construire l'image depuis le code source de l'application.

### Construire l'image depuis le projet local (avec Dockerfile)

En amont de la construction de l'image Docker depuis le code source, il faut packager le projet en utilisant la commande `mvn clean package`.  
Un dossier `/target` contenant le `.jar` sera généré et utilisé pour créer l'image du service de vérification.

```bash
docker build 
--file Dockerfile.local
--tag <image_name>:<version> . 
```

- **image_name** : le libelle de l'image Docker pour le service de vérification

### Démarrage du service de vérification

Pour démarrer le service de vérification, il faut utiliser la commande `docker run` en spécifiant certains paramètres.

La configuration minimale peut être utilisée pour démarrer le service de vérification avec un minimum de paramétrage, la valeur par défaut des variables d'environnement non paramétrée sera utilisée, voir [valeurs par défaut](#variables-environnement).  

La configuration complète permet d'exécuter le service de vérification avec l'ensemble des variables d'environnement paramétrées.

Se référer à la section [Variables d'environnement](#variables-environnement) pour la documentation de l'ensemble des paramètres d'exécution.

#### Configuration minimale

##### Vérifier attestations générées depuis production
```bash
docker run --rm -p <port_publish>:8080
--name <container_name> <image_name>:<version>
```

##### Vérifier attestations générées depuis qualification, préproduction
```bash
docker run --rm -p <port_publish>:8080 
--env SGIN_SIGNATURE_VERIFY_HASH=false 
--env DSS_EXTRA_CA_CERTS=certificates/qualif_ac_root_minint_2018.crt,certificates/qualif_server_1E_2018.crt
--name <container_name> <image_name>:<version>
```

#### Configuration complète
```bash
docker run --rm -p <port_publish>:8080
--env ROOT_LEVEL=INFO
--env DSS_TRUSTSTORE_TYPE=JKS
--env DSS_TRUSTSTORE_FILE=TLTrustStore.jks
--env DSS_TRUSTSTORE_PASSWORD=<truststore_password>
--env DSS_TL_URL=http://www.ssi.gouv.fr/eidas/TL-FR.xml
--env DSS_TL_PROXY_ENABLE=<proxy_enable>
--env DSS_TL_PROXY_HOST=<proxy_host>
--env DSS_TL_PROXY_PORT=<proxy_port>
--env DSS_DL_CONNECTION_TIMEOUT=5000
--env DSS_DL_CONNECTION_REQ_TIMEOUT=5000
--env DSS_DL_REDIRECT_ENABLED=true
--env DSS_CRON_TL_LOADER_ENABLE=true
--env DSS_CRON_TL_LOADER_INIT_DELAY=0
--env DSS_CRON_TL_LOADER_DELAY=3600
--env DSS_CACHE_TL_EXPIRATION_TIME_MIN=1200
--env DSS_CACHE_OCSP_EXPIRATION_TIME_MIN=1200
--env DSS_CACHE_CRL_EXPIRATION_TIME_MIN=1200
--env DSS_CACHE_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=20000
--env DSS_CACHE_DATASOURCE_HIKARI_MAX_POOL_SIZE=20
--env SGIN_SIGNATURE_VERIFY_HASH=true
--env SGIN_SIGNATURE_POLICY_ID=1.2.250.1.152.202.1.1.1
--env SGIN_SIGNATURE_PATTERN_ORG_UNIT=0002110014016
--env SGIN_SIGNATURE_PATTERN_ORG_IDENTIFIER=NTRFR-110014016
--env SGIN_FILE_MAX_SIZE_KB=500
--env SGIN_GET_REPORTS=false
--env DSS_DL_SSL_PROTOCOL=TLSv1.3
--name <container_name> <image_name>:<version>
```
- **port_publish** : le port de la machine locale partagé avec celui du container Docker
- **truststore_password** : le mot de passe du TrustStore
- **proxy_enable** : activation ou désactivation de l'utilisation d'un proxy, utilisé notamment pour le téléchargement de la liste des certificats révoqués
- **proxy_host** : nom de domaine du proxy
- **proxy_port** : port du proxy
- **container_name** : le libelle du container dans lequel sera exécuté le service de vérification
- **image_name** : le libelle de l'image Docker du service de vérification
- **version** : la version de l'image du service

### Variables environnement

#### Variables fonctionnelles

| Paramètre                                        | Description                                                                            |  Valeur par défaut                        |
|--------------------------------------------------|----------------------------------------------------------------------------------------|-------------------------------------------|
| ROOT_LEVEL                                       | Permet de spécifier le niveau de log de façon globale au microservice                  | INFO                                      |
| DSS_TL_URL                                       | Url des trustedList                                                                    | http://www.ssi.gouv.fr/eidas/TL-FR.xml    |
| DSS_TRUSTSTORE_FILE                              | Libelle du fichier truststore                                                          | TLTrustStore.jks                          |
| DSS_TRUSTSTORE_TYPE                              | Type de fichier truststore                                                             | JKS                                       |
| DSS_TRUSTSTORE_PASSWORD                          | Mot de passe d'accès au fichier truststore                                             | xxxxx                                     |
| SGIN_SIGNATURE_POLICY_ID                         | L'identifiant de la politique de signature à comparer`                                 | 1.2.250.1.152.202.1.1.1                   |
| SGIN_SIGNATURE_PATTERN_ORG_UNIT                  | OrganizationnalUnit du sujet à comparer                                                | 0002110014016                             |
| SGIN_SIGNATURE_PATTERN_ORG_IDENTIFIER            | OrganizationIdentifier du sujet à comparer `                                           | NTRFR-110014016                           |

#### Variables techniques

| Paramètre                                      | Description                                                                                         | Valeur par défaut |
|------------------------------------------------|-----------------------------------------------------------------------------------------------------|-------------------|
| SGIN_SIGNATURE_VERIFY_HASH                     | Paramétrage de la vérification ou non du hash de la signature de l'attestation **Toujours laisser à "true" en environnement de production** | true              |
| DSS_CACHE_DATASOURCE_HIKARI_CONNECTION_TIMEOUT | Timeout connexion Hikari (milliseconds)                                                             | 20000             |
| DSS_CACHE_DATASOURCE_HIKARI_MAX_POOL_SIZE      | Taille max du pool Hikari                                                                           | 20                |
| DSS_TL_PROXY_ENABLE                            | Active/Désactive le proxy                                                                           | false             |
| DSS_TL_PROXY_HOST                              | Host du proxy                                                                                       | null              |
| DSS_TL_PROXY_PORT                              | Port du proxy                                                                                       | 8080              |
| DSS_DL_CONNECTION_TIMEOUT                      | Timeout de connexion des dataLoaders (milliseconds)                                                 | 5000              |
| DSS_DL_CONNECTION_REQ_TIMEOUT                  | Timeout des requêtes des dataLoaders (milliseconds)                                                 | 5000              |
| DSS_DL_REDIRECT_ENABLED                        | Autorise/Refuse les redirections des dataLoaders                                                    | true              |
| DSS_CRON_TL_LOADER_ENABLE                      | Active/Desactive le job permettant de télécharger, parser et valider les trusted list régulièrement | true              |
| DSS_CRON_TL_LOADER_INIT_DELAY                  | Delais initial de rafraichissement des trusted list                                                 | 0                 |
| DSS_CRON_TL_LOADER_DELAY                       | Delais de rafraichissement des trusted list (seconds)                                               | 3600              |
| DSS_CACHE_TL_EXPIRATION_TIME_MIN               | Durée avant expiration des trusted list, mise en cache (minutes)                                    | 1200              |
| DSS_CACHE_OCSP_EXPIRATION_TIME_MIN             | Durée avant expiration de la liste des certificats révoqués OCSP, mise en cache (minutes)           | 1200              |
| DSS_CACHE_CRL_EXPIRATION_TIME_MIN              | Durée avant expiration de la liste des certificats révoqués CRL, mise en cache (minutes)            | 1200              |
| SGIN_FILE_MAX_SIZE_KB                          | Taille max des fichiers PDF pour validation (kB)                                                    | 500               |
| SGIN_GET_REPORTS                               | Remonte ou non les rapports de validation (détaillé et diagnostic)                                  | false             |
| DSS_DL_SSL_PROTOCOL                            | Le protocole SSL à utiliser pour charger la trustedList                                             | TLSv1.3           |
| DSS_DL_SSL_PROTOCOLS_SUPPORTED                 | Les protocoles SSL supportés pour le chargement de la trustedList                                   | /                 |
| DSS_EXTRA_CA_CERTS                             | Chemins des certificats AC supplémentaire pour validation des attestations hors production **Ne pas spécifier en environnement de production** | /                 |

> **Vérification attestations de production**  
> - _DSS_EXTRA_CA_CERTS_ toujours vides  
> - _SGIN_SIGNATURE_VERIFY_HASH=true_

> **Vérification attestations de qualification, préproduction**  
> - _DSS_EXTRA_CA_CERTS_ toujours renseignés  
> - _SGIN_SIGNATURE_VERIFY_HASH=false_

## Documentation des APIs

Le service de vérification des justificatifs d'identité utilise la librairie _Open API Swagger_ pour générer la documentation des APIs.

Une fois le service de vérification en cours d'exécution dans le container Docker, 
la documentation est accessible sous :
```
http://<host_url>:<port_publish>/api/swagger-ui/index.html
```
*(Exemple: http://localhost:8080/api/swagger-ui/index.html)*

- **host_url** : le nom de domaine sur lequel est exposé le service de vérification.
- **port_publish** : le port de la machine locale partagé avec celui du container Docker.

## Releases

### attestation-validator-api-1.0.0

```
- Initial project
```


