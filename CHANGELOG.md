# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Latest]

## [3.1.0]

### Added
- UUIDs can now be serialized to representation.

## [3.0.1]

### Changed

- Publishing to maven central automated via Gradle plugin
- RingElementVector are now UniqueByteRepresentable [PR](https://github.com/cryptimeleon/math/pull/139)

### Fixed
- Concatenation in class Vector now works as intended [PR](https://github.com/cryptimeleon/math/pull/138)

## [3.0.0]

### Changed
- `DebugGroup` group operation counting data is now split up into buckets that allow, for example, to separately count operations done by different parties in an interactive protocol. Furthermore, counting is now done statically, i.e. the data in each bucket persists across `DebugGroup` instances.
- Reduce collisions for `Zn#injectiveValueOf`

### Added
- Add lazy and naive wrappers around `Secp256k1` curve, and make curve implementation package-private

### Fixed
- Fixed [issue](https://github.com/cryptimeleon/math/pull/134) where exceptions during group computations could hang up the whole applications without surfacing the exception.

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


[Latest]: https://github.com/cryptimeleon/math/compare/v3.1.0...HEAD
[3.1.0]: https://github.com/cryptimeleon/math/compare/v3.0.0...v3.0.1
[3.0.1]: https://github.com/cryptimeleon/math/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/cryptimeleon/math/compare/v2.1.0...v3.0.0
[2.1.0]: https://github.com/cryptimeleon/math/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/cryptimeleon/math/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/cryptimeleon/math/releases/tag/v1.0.0
