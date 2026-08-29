import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:trace_mobile/app/providers.dart';
import 'package:trace_mobile/features/auth/presentation/auth_controller.dart';

class RegisterScreen extends ConsumerStatefulWidget {
  const RegisterScreen({super.key});

  @override
  ConsumerState<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends ConsumerState<RegisterScreen> {
  final _name = TextEditingController();
  final _email = TextEditingController();
  final _password = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _name.dispose();
    _email.dispose();
    _password.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final busy = ref.watch(authControllerProvider).status == AuthStatus.busy;
    return Scaffold(
      appBar: AppBar(title: const Text('Tạo tài khoản')),
      body: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          Form(
            key: _formKey,
            child: Column(
              children: [
                TextFormField(
                  controller: _name,
                  decoration: const InputDecoration(labelText: 'Tên hiển thị'),
                  validator: (value) =>
                      (value?.trim().length ?? 0) >= 2 ? null : 'Tên quá ngắn',
                ),
                const SizedBox(height: 14),
                TextFormField(
                  controller: _email,
                  keyboardType: TextInputType.emailAddress,
                  decoration: const InputDecoration(labelText: 'Email'),
                  validator: (value) => value != null && value.contains('@')
                      ? null
                      : 'Email không hợp lệ',
                ),
                const SizedBox(height: 14),
                TextFormField(
                  controller: _password,
                  obscureText: true,
                  decoration: const InputDecoration(labelText: 'Mật khẩu'),
                  validator: (value) => (value?.length ?? 0) >= 10
                      ? null
                      : 'Cần ít nhất 10 ký tự',
                ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: busy ? null : _submit,
                    child: const Text('Đăng ký'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final success = await ref
        .read(authControllerProvider.notifier)
        .register(
          email: _email.text,
          password: _password.text,
          displayName: _name.text,
        );
    if (success && mounted) Navigator.of(context).pop();
  }
}
