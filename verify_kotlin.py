#!/usr/bin/env python3
"""
Android Kotlin 프로젝트 Dry-Run 검증 스크립트
Android SDK 없이 최대한 검증합니다.
"""
import os
import re
import sys
from pathlib import Path
from typing import List, Dict, Set, Tuple

class AndroidDryRun:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.app_src = self.project_root / "app" / "src" / "main"
        self.errors = []
        self.warnings = []
        self.success_count = 0

    def verify_all(self):
        print("🔍 Android 프로젝트 Dry-Run 검증 시작...\n")

        # 1. 필수 파일 존재 확인
        print("📁 Step 1: 필수 파일 검증")
        self.check_required_files()

        # 2. Kotlin 파일 구문 검증
        print("\n📝 Step 2: Kotlin 파일 구문 검증")
        self.verify_kotlin_files()

        # 3. AndroidManifest.xml 검증
        print("\n📱 Step 3: AndroidManifest.xml 검증")
        self.verify_manifest()

        # 4. build.gradle.kts 검증
        print("\n🔧 Step 4: build.gradle.kts 검증")
        self.verify_gradle_files()

        # 5. 패키지 구조 검증
        print("\n📦 Step 5: 패키지 구조 검증")
        self.verify_package_structure()

        # 6. Import 경로 검증
        print("\n🔗 Step 6: Import 경로 검증")
        self.verify_imports()

        # 결과 출력
        self.print_summary()

    def check_required_files(self):
        required_files = [
            "app/build.gradle.kts",
            "build.gradle.kts",
            "settings.gradle.kts",
            "gradle.properties",
            "app/src/main/AndroidManifest.xml",
        ]

        for file_path in required_files:
            full_path = self.project_root / file_path
            if full_path.exists():
                print(f"  ✓ {file_path}")
                self.success_count += 1
            else:
                self.errors.append(f"필수 파일 누락: {file_path}")
                print(f"  ✗ {file_path}")

    def verify_kotlin_files(self):
        kotlin_files = list(self.app_src.glob("**/*.kt"))
        print(f"  총 {len(kotlin_files)}개 Kotlin 파일 검증 중...")

        for kt_file in kotlin_files:
            try:
                self.verify_single_kotlin_file(kt_file)
            except Exception as e:
                self.errors.append(f"{kt_file.name}: {str(e)}")

    def verify_single_kotlin_file(self, file_path: Path):
        content = file_path.read_text()

        # 패키지 선언 확인
        if not re.search(r'^package\s+[\w\.]+', content, re.MULTILINE):
            self.warnings.append(f"{file_path.name}: 패키지 선언 없음")

        # 기본 구문 오류 체크
        # 1. 중괄호 매칭
        open_braces = content.count('{')
        close_braces = content.count('}')
        if open_braces != close_braces:
            self.errors.append(f"{file_path.name}: 중괄호 불일치 ({{ {open_braces} vs }} {close_braces})")
            return

        # 2. 괄호 매칭
        open_parens = content.count('(')
        close_parens = content.count(')')
        if open_parens != close_parens:
            self.errors.append(f"{file_path.name}: 괄호 불일치 (( {open_parens} vs ) {close_parens})")
            return

        # 3. 클래스/함수 선언 확인
        has_class = bool(re.search(r'\b(class|interface|object|enum class|data class|sealed class)\b', content))
        has_function = bool(re.search(r'\bfun\s+\w+', content))

        if not has_class and not has_function:
            self.warnings.append(f"{file_path.name}: 클래스나 함수 선언이 없음")

        # 4. Import 중복 체크
        imports = re.findall(r'^import\s+([\w\.]+)', content, re.MULTILINE)
        if len(imports) != len(set(imports)):
            duplicates = [imp for imp in imports if imports.count(imp) > 1]
            self.warnings.append(f"{file_path.name}: 중복 import: {set(duplicates)}")

        self.success_count += 1

    def verify_manifest(self):
        manifest_path = self.app_src / "AndroidManifest.xml"
        if not manifest_path.exists():
            self.errors.append("AndroidManifest.xml 파일이 없습니다")
            return

        content = manifest_path.read_text()

        # 필수 요소 체크
        checks = [
            (r'<manifest', "manifest 태그"),
            (r'package="[\w\.]+"', "package 선언"),
            (r'<application', "application 태그"),
            (r'android:name="\.MemoirApplication"', "Application 클래스"),
        ]

        for pattern, desc in checks:
            if re.search(pattern, content):
                print(f"  ✓ {desc}")
                self.success_count += 1
            else:
                self.warnings.append(f"AndroidManifest.xml: {desc} 확인 필요")
                print(f"  ⚠ {desc}")

    def verify_gradle_files(self):
        app_gradle = self.project_root / "app" / "build.gradle.kts"

        if not app_gradle.exists():
            self.errors.append("app/build.gradle.kts 파일이 없습니다")
            return

        content = app_gradle.read_text()

        # 필수 플러그인 체크
        plugins = [
            ("com.android.application", "Android Application Plugin"),
            ("org.jetbrains.kotlin.android", "Kotlin Android Plugin"),
            ("com.google.dagger.hilt.android", "Hilt Plugin"),
        ]

        for plugin_id, desc in plugins:
            if plugin_id in content:
                print(f"  ✓ {desc}")
                self.success_count += 1
            else:
                self.errors.append(f"build.gradle.kts: {desc} 누락")
                print(f"  ✗ {desc}")

        # 필수 의존성 체크
        dependencies = [
            ("androidx.compose.ui:ui", "Jetpack Compose UI"),
            ("androidx.compose.material3:material3", "Material 3"),
            ("com.google.dagger:hilt-android", "Hilt DI"),
            ("com.squareup.retrofit2:retrofit", "Retrofit"),
            ("androidx.room:room-runtime", "Room Database"),
        ]

        for dep, desc in dependencies:
            if dep in content:
                print(f"  ✓ {desc}")
                self.success_count += 1
            else:
                self.warnings.append(f"build.gradle.kts: {desc} 확인 필요")
                print(f"  ⚠ {desc}")

    def verify_package_structure(self):
        expected_packages = [
            "data/local/database",
            "data/local/datastore",
            "data/remote/api",
            "data/remote/dto",
            "data/repository",
            "domain/model",
            "domain/repository",
            "domain/usecase",
            "presentation/auth",
            "presentation/onboarding",
            "presentation/profile",
            "presentation/navigation",
            "presentation/ui/theme",
            "di",
            "util",
        ]

        for package in expected_packages:
            package_path = self.app_src / "java" / "com" / "memoir" / "app" / package.replace("/", os.sep)
            if package_path.exists() and any(package_path.glob("*.kt")):
                print(f"  ✓ {package}")
                self.success_count += 1
            else:
                self.warnings.append(f"패키지 누락 또는 빈 폴더: {package}")
                print(f"  ⚠ {package}")

    def verify_imports(self):
        """Import 경로가 실제 파일과 일치하는지 검증"""
        kotlin_files = list(self.app_src.glob("**/*.kt"))

        # 모든 클래스 파일의 패키지 + 클래스명 수집
        available_classes = set()
        for kt_file in kotlin_files:
            content = kt_file.read_text()
            package_match = re.search(r'^package\s+([\w\.]+)', content, re.MULTILINE)
            if package_match:
                package = package_match.group(1)

                # 클래스 이름 추출
                class_matches = re.finditer(
                    r'\b(class|interface|object|enum class|data class|sealed class)\s+(\w+)',
                    content
                )
                for match in class_matches:
                    class_name = match.group(2)
                    available_classes.add(f"{package}.{class_name}")

        print(f"  프로젝트 내 클래스: {len(available_classes)}개")

        # Import 검증
        unresolved_imports = set()
        for kt_file in kotlin_files:
            content = kt_file.read_text()
            imports = re.findall(r'^import\s+(com\.memoir\.app\.[\w\.]+)', content, re.MULTILINE)

            for imp in imports:
                # 와일드카드 import는 제외
                if imp.endswith('.*'):
                    continue

                # 프로젝트 내부 import인지 확인
                if not any(imp.startswith(cls.rsplit('.', 1)[0]) or imp == cls for cls in available_classes):
                    if imp not in unresolved_imports:
                        unresolved_imports.add(imp)

        if unresolved_imports:
            print(f"  ⚠ 확인 필요한 import: {len(unresolved_imports)}개")
            for imp in list(unresolved_imports)[:5]:  # 처음 5개만 표시
                self.warnings.append(f"Import 확인 필요: {imp}")
        else:
            print(f"  ✓ 모든 프로젝트 내부 import 경로 확인")
            self.success_count += 1

    def print_summary(self):
        print("\n" + "="*60)
        print("📊 검증 결과 요약")
        print("="*60)
        print(f"✅ 성공: {self.success_count}개")
        print(f"⚠️  경고: {len(self.warnings)}개")
        print(f"❌ 오류: {len(self.errors)}개")

        if self.errors:
            print("\n❌ 오류 목록:")
            for error in self.errors:
                print(f"  - {error}")

        if self.warnings:
            print("\n⚠️  경고 목록:")
            for warning in self.warnings[:10]:  # 처음 10개만
                print(f"  - {warning}")
            if len(self.warnings) > 10:
                print(f"  ... 외 {len(self.warnings) - 10}개")

        print("\n" + "="*60)
        if len(self.errors) == 0:
            print("✅ Dry-Run 검증 통과! (경고는 있을 수 있음)")
            print("   → Android Studio에서 빌드 시도 권장")
            return 0
        else:
            print("❌ Dry-Run 검증 실패")
            print("   → 오류를 수정해야 합니다")
            return 1

if __name__ == "__main__":
    verifier = AndroidDryRun("/home/user/memoir")
    sys.exit(verifier.verify_all())
