#!/usr/bin/env bash
set -euo pipefail

output="$PWD/build/ios"
mkdir -p "$output/export"
keychain="$RUNNER_TEMP/pingly-signing.keychain-db"
profile_path=""
cleanup() {
  if [[ -f "$keychain" ]]; then security delete-keychain "$keychain"; fi
  if [[ -n "$profile_path" ]]; then rm -f "$profile_path"; fi
  rm -f "$RUNNER_TEMP/pingly.p12" "$RUNNER_TEMP/pingly.mobileprovision" "$RUNNER_TEMP/pingly-profile.plist"
}
trap cleanup EXIT

signing=(CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO)
if [[ "${SIGNED_IOS:-false}" == true ]]; then
  : "${IOS_TEAM_ID:?Set the IOS_TEAM_ID repository variable}"
  : "${IOS_CERTIFICATE_BASE64:?Set the IOS_CERTIFICATE_BASE64 secret}"
  : "${IOS_CERTIFICATE_PASSWORD:?Set the IOS_CERTIFICATE_PASSWORD secret}"
  : "${IOS_PROFILE_BASE64:?Set the IOS_PROFILE_BASE64 secret}"
  printf '%s' "$IOS_CERTIFICATE_BASE64" | base64 --decode > "$RUNNER_TEMP/pingly.p12"
  printf '%s' "$IOS_PROFILE_BASE64" | base64 --decode > "$RUNNER_TEMP/pingly.mobileprovision"
  password="$(openssl rand -hex 24)"
  security create-keychain -p "$password" "$keychain"
  security set-keychain-settings -lut 21600 "$keychain"
  security unlock-keychain -p "$password" "$keychain"
  security import "$RUNNER_TEMP/pingly.p12" -P "$IOS_CERTIFICATE_PASSWORD" -A -t cert -f pkcs12 -k "$keychain"
  security set-key-partition-list -S apple-tool:,apple: -k "$password" "$keychain" >/dev/null
  security list-keychains -d user -s "$keychain" "$HOME/Library/Keychains/login.keychain-db"
  security cms -D -i "$RUNNER_TEMP/pingly.mobileprovision" > "$RUNNER_TEMP/pingly-profile.plist"
  uuid=$(/usr/libexec/PlistBuddy -c 'Print UUID' "$RUNNER_TEMP/pingly-profile.plist")
  profile_path="$HOME/Library/MobileDevice/Provisioning Profiles/$uuid.mobileprovision"
  mkdir -p "$(dirname "$profile_path")"
  cp "$RUNNER_TEMP/pingly.mobileprovision" "$profile_path"
  signing=(CODE_SIGN_STYLE=Manual CODE_SIGN_IDENTITY="Apple Distribution" "DEVELOPMENT_TEAM=$IOS_TEAM_ID" "PROVISIONING_PROFILE_SPECIFIER=$uuid" "OTHER_CODE_SIGN_FLAGS=--keychain $keychain")
fi

xcodebuild -version
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -configuration Release -destination 'generic/platform=iOS' \
  -archivePath "$output/Pingly.xcarchive" \
  "PRODUCT_BUNDLE_IDENTIFIER=$IOS_BUNDLE_ID" \
  "CURRENT_PROJECT_VERSION=$GITHUB_RUN_NUMBER" \
  "${signing[@]}" archive

if [[ "${SIGNED_IOS:-false}" == true ]]; then
  export PROFILE_UUID="$uuid"
  python3 - <<'PY'
import os, plistlib
with open('build/ios/ExportOptions.plist', 'wb') as f:
    plistlib.dump({
        'method': os.environ['IOS_EXPORT_METHOD'],
        'teamID': os.environ['IOS_TEAM_ID'],
        'signingStyle': 'manual',
        'signingCertificate': 'Apple Distribution',
        'provisioningProfiles': {os.environ['IOS_BUNDLE_ID']: os.environ['PROFILE_UUID']},
    }, f)
PY
  xcodebuild -exportArchive -archivePath "$output/Pingly.xcarchive" \
    -exportOptionsPlist "$output/ExportOptions.plist" -exportPath "$output/export"
else
  mkdir -p "$output/package/Payload"
  cp -R "$output/Pingly.xcarchive/Products/Applications/Pingly.app" "$output/package/Payload/"
  (cd "$output/package" && /usr/bin/zip -qry "$output/export/Pingly-unsigned.ipa" Payload)
fi
