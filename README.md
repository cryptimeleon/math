![Build Status](https://github.com/cryptimeleon/math/workflows/Development%20Java%20CI/badge.svg)
![Build Status](https://github.com/cryptimeleon/math/workflows/Main%20Java%20CI/badge.svg)
![Build Status](https://github.com/cryptimeleon/math/workflows/Scheduled%20Main%20Java%20CI/badge.svg)
## Math

The Cryptimeleon Math library provides the mathematical foundation for the other Cryptimeleon libraries.
It implements basics such as mathematical groups, rings and fields, e.g. Zn, as well as implementations of cryptographic pairings.
Furthermore, it offers serialization support for the implemented structures.

## Security Disclaimer
**WARNING: This library is meant to be used for prototyping and as a research tool *only*. It has not been sufficiently vetted for use in security-critical production environments. All implementations are to be considered experimental.**

## Table Of Contents

* [Features Overview](#features)
* [Quickstart Guide](#quickstart)
    * [Maven Installation](#installation-with-maven)
    * [Gradle Installation](#installation-with-gradle)
    * [Tutorials](#tutorials)
* [Pairing Performance](#note-regarding-pairing-performance)
* [Miscellaneous Information](#miscellaneous-information)
* [Authors](#authors)

## Features

Below we give a more detailed list of features.

### Groups

Math offers the following algebraic groups:

* Bilinear groups:
    * Type 1 and type 3 pairings
* Elliptic curves without pairings:
    * `Secp256k1`
* Symmetric group Sn
* Cartesian product group

### Rings

Math offers the following algebraic rings and fields:

* Boolean ring
* Cartesian product ring
* Field extension class for polynomials of the form x^d + c
* Integer ring
* Polynomial ring
* Ring Zn and Field Zp for prime p

### Other Features

Math also implements a number of other features:

* Multi-exponentiation algorithms
* Deferred evaluation of group operations for automatic application of those multi-exponentiation algorithms
* Serialization features that integrate with the implemented algebraic structures
* Group operation counting capabilities
* A random generator
* Hash function implementations such as SHA256 and SHA512

## Quickstart

### Installation With Maven
To add the newest Math version as a dependency, add this to your project's POM:

```xml
<dependency>
    <groupId>org.cryptimeleon</groupId>
    <artifactId>math</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Installation With Gradle

Math is published via Maven Central.
Therefore, you need to add `mavenCentral()` to the `repositories` section of your project's `build.gradle` file.
Then, add `implementation group: 'org.cryptimeleon', name: 'math', version: '3.0.0'` to the `dependencies` section of your `build.gradle` file.

For example:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation group: 'org.cryptimeleon', name: 'math', version: '3.0.0'
}
```

### Tutorials

We recommend you go through our [short Math tutorial](https://cryptimeleon.github.io/getting-started/5-minute-tutorial.html) to get started.

We also provide a walkthrough where we show you how to implement a pairing-based signature scheme [here](https://cryptimeleon.github.io/getting-started/pairing-tutorial.html).
    
## Note Regarding Pairing Performance

The included java pairings are not optimized for performance.
We recommend you use our [Mcl wrapper library](https://github.com/cryptimeleon/mclwrap) if you care about pairing performance. 
It includes an optimized type 3 pairing.

## Miscellaneous Information

- Official Documentation can be found [here](https://cryptimeleon.github.io/).
    - The *For Contributors* area includes information on how to contribute.
- Math adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
- The changelog can be found [here](CHANGELOG.md).
- Math is licensed under Apache License 2.0, see [LICENSE file](LICENSE).

## Authors
The library was implemented at Paderborn University in the research group ["Codes und Cryptography"](https://cs.uni-paderborn.de/en/cuk/).
