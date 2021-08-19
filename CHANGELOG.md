# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Reduce collisions for `Zn#injectiveValueOf`

### Added
- Add lazy and naive wrappers around `Secp256k1` curve, and make curve implementation package-private


## [2.1.0]

### Added
- Algorithm selection for debug groups

### Fixed
- `IntegerRing.decomposeIntoDigits` sometimes worked incorrectly

## [2.0.0] - 2021-06-23

### Added
- New indifferentiable hash functions to G1 and G2 for Barreto-Naehrig bilinear groups
- Additional operator overload methods added to `ExponentExpr`
- `BasicBilinearGroup` wrappers for the implemented bilinear groups
- Convenience methods for the vector classes
- `square`, `div` and `valueOf` convenience methods for `Zn` and `Zp` classes
- PRF classes from Craco now are part of Math
- `ByteArrayImpl`, a byte array implementation

### Changed
- Renamed "counting" group classes and package to "debug"
- Made supersingular and Barreto-Naehrig implementation classes package-private (except those that are relevant to the user)
- Made internal lazy group classes package-private
- Adjusted some tests to work with the new package-private classes
- Made representation handler classes package-private and moved the classes up a package
- Made ring group impl classes package-private and moved inv and neg cost estimation to the `Ring` interface
- Improved performance of finite field multiplication

### Fixed
- Fixed `decomposeIntoDigits` method of `IntegerRing`

## [1.0.0] - 2021-03-01

### Added
- Initial release


[Unreleased]: https://github.com/cryptimeleon/math/compare/v2.1.0...HEAD
[2.1.0]: https://github.com/cryptimeleon/math/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/cryptimeleon/math/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/cryptimeleon/math/releases/tag/v1.0.0
