[![Build Status](https://travis-ci.com/upbcuk/upb.crypto.math.svg?branch=master)](https://travis-ci.com/upbcuk/upb.crypto.math)
## upb.crypto.math

**WARNING: this library is meant to be used for prototyping and as a research tool *only*. It has not been sufficiently vetted to use in production.**

upb.crypto.math is a library providing a number of mathematical tools needed to prototype many cryptographic applications.

These include:

* Elliptic curve groups with pairings
    * Type 1:
        * Supersingular Curve with Tate pairing
    * Type 3: 
        * Barreto-Naehrig
* Hashing
    * SHA-256
    * SHA-512
* Mathematical structures:
    * Ring of integers modulo n
    * Ring of polynomials
    
## Hints
* The included java pairing is not optimized for performance
* Please use your own or one of our provided wrappers to a more performant pairing library (see [below](#bilinear_group)).
    
## Example Code
    
As a starting point, we provide exemplary code of common tasks.

##### Setting up a Type 3 Bilinear Group <a name="bilinear_group"></a>

Given a security parameter `securityParameter`, we can set up a type 3 bilinear group using this library as follows:

```java
BilinearGroupFactory fac = new BilinearGroupFactory(securityParameter);
fac.setRequirements(BilinearGroup.Type.TYPE_3);
BilinearGroup group = fac.createBilinearGroup();
``` 

This chooses a type 3 bilinear group from predefined ones. Alternatively, the library enables it to register new groups by defining a `BilinearGroupProvider`.

##### Register your own Bilinear Group Implementation

Suppose you have your own implementation of a type 3 bilinear group and you want to use it in our library. To do so, you only need write a `MyBilinearGroupProvider` that implements the interface `BilinearGroupProvider`.
Then, your group can be registered in the `BilinearGroupFactory` as follows:

```java
BilinearGroupFactory fac = new BilinearGroupFactory(securityParameter);
fac.registerProvider(Arrays.asList(new BarretoNaehrigProvider(), new MyBilinearGroupProvider()));
fac.setRequirements(BilinearGroup.Type.TYPE_3);
BilinearGroup group = fac.createBilinearGroup();
```

As an example have a look at our module [upb.crypto.mclwrap](https://github.com/upbcuk/upb.crypto.mclwrap), which includes the pairing library [mcl](https://github.com/herumi/mcl) in our environment.

## Notes

The library was implemented at Paderborn University in the research group ["Codes und Cryptography"](https://cs.uni-paderborn.de/en/cuk/).

This module is the base of [CRACO](https://github.com/upbcuk/upb.crypto.craco) and [CLARC](https://github.com/upbcuk/upb.crypto.clarc) providing cryptographic constructions, and an anonymous credential and reputation system, respectively. 

## Licence
Apache License 2.0, see LICENCE file.
